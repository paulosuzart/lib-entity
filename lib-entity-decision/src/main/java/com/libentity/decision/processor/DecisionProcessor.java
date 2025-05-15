package com.libentity.decision.processor;

import com.libentity.decision.DecisionInput;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.WildcardTypeName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@SupportedAnnotationTypes({
    "com.libentity.decision.DecisionInput",
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class DecisionProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(DecisionInput.class)) {
            TypeElement typeElement = (TypeElement) element;
            try {
                generateUnwrappedClass(typeElement);
                generateRuleProviderClass(typeElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void generateUnwrappedClass(TypeElement typeElement) throws IOException {
        String packageName = processingEnv
                .getElementUtils()
                .getPackageOf(typeElement)
                .getQualifiedName()
                .toString();
        String className = typeElement.getSimpleName() + "Value";
        TypeSpec.Builder classBuilder = TypeSpec.recordBuilder(className).addModifiers(Modifier.PUBLIC);

        List<ParameterSpec> components = new ArrayList<>();
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosed;
                TypeMirror fieldType = field.asType();
                // Assume fieldType is RuleIn<T>, extract T
                TypeMirror unwrappedType = extractGenericType(fieldType);
                if (unwrappedType != null) {
                    ParameterSpec fieldSpec = ParameterSpec.builder(
                                    TypeName.get(unwrappedType),
                                    field.getSimpleName().toString())
                            .build();
                    components.add(fieldSpec);
                }
            }
        }
        MethodSpec methodSpec =
                MethodSpec.constructorBuilder().addParameters(components).build();
        classBuilder.recordConstructor(methodSpec);

        // Generate getters/setters or use Lombok annotations
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        javaFile.writeTo(processingEnv.getFiler());
    }

    //    // Generate <OriginalClass>RuleProvider
    //    private void generateRuleProviderClass(TypeElement typeElement) throws IOException {
    //        String packageName = processingEnv
    //                .getElementUtils()
    //                .getPackageOf(typeElement)
    //                .getQualifiedName()
    //                .toString();
    //        String className = typeElement.getSimpleName() + "RuleProvider";
    //        String inputClassName = typeElement.getSimpleName().toString();
    //        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);
    //
    //        // Collect field names
    //        List<String> fieldNames = new ArrayList<>();
    //        for (Element enclosed : typeElement.getEnclosedElements()) {
    //            if (enclosed.getKind() == ElementKind.FIELD) {
    //                VariableElement field = (VariableElement) enclosed;
    //                fieldNames.add(field.getSimpleName().toString());
    //            }
    //        }
    //
    //        // Generate static getRules(Input input) method
    //        MethodSpec getRules = MethodSpec.methodBuilder("getRules")
    //                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    //                .addParameter(ClassName.get(packageName, inputClassName), "input")
    //                .returns(ParameterizedTypeName.get(
    //                        ClassName.get(List.class),
    //                        ParameterizedTypeName.get(
    //                                ClassName.get("com.libentity.decision", "Rule"),
    //                                WildcardTypeName.subtypeOf(ClassName.get("java.lang", "Object")))))
    //                .addStatement(
    //                        "return $T.of($L)",
    //                        ClassName.get(List.class),
    //                        fieldNames.stream().map(name -> "input." + name).collect(Collectors.joining(", ")))
    //                .build();
    //        classBuilder.addMethod(getRules);
    //
    //        // Define I<Input> interface type
    //        TypeName inputType = ClassName.get(packageName, inputClassName);
    //        ParameterizedTypeName interfaceType =
    //                ParameterizedTypeName.get(ClassName.get("com.libentity.decision", "I"), inputType);
    //
    //        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
    //        javaFile.writeTo(processingEnv.getFiler());
    //    }

    private void generateRuleProviderClass(TypeElement typeElement) throws IOException {
        String packageName = processingEnv
                .getElementUtils()
                .getPackageOf(typeElement)
                .getQualifiedName()
                .toString();
        String className = typeElement.getSimpleName() + "RuleProvider";
        String inputClassName = typeElement.getSimpleName().toString();
        String unwrappedClassName = typeElement.getSimpleName() + "Value";

        // Collect field names and their generic types
        List<String> fieldNames = new ArrayList<>();
        List<TypeName> fieldGenericTypes = new ArrayList<>();
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosed;
                TypeMirror fieldType = field.asType();
                if (fieldType instanceof DeclaredType declaredType) {
                    if (declaredType.asElement().getSimpleName().toString().equals("Rule")) {
                        List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
                        if (!typeArgs.isEmpty()) {
                            fieldNames.add(field.getSimpleName().toString());
                            fieldGenericTypes.add(TypeName.get(typeArgs.get(0)));
                        } else {
                            processingEnv
                                    .getMessager()
                                    .printMessage(
                                            javax.tools.Diagnostic.Kind.WARNING,
                                            "Skipping field " + field.getSimpleName() + ": Rule has no type arguments",
                                            field);
                        }
                    } else {
                        processingEnv
                                .getMessager()
                                .printMessage(
                                        javax.tools.Diagnostic.Kind.WARNING,
                                        "Skipping field " + field.getSimpleName() + ": expected Rule type, found "
                                                + fieldType,
                                        field);
                    }
                }
            }
        }

        // Define types
        TypeName inputType = ClassName.get(packageName, inputClassName);
        TypeName valueType = ClassName.get(packageName, unwrappedClassName);
        ParameterizedTypeName interfaceType = ParameterizedTypeName.get(
                ClassName.get("com.libentity.decision", "InputProvider"), inputType, valueType);
        ParameterizedTypeName ruleType = ParameterizedTypeName.get(
                ClassName.get("com.libentity.decision", "Rule"),
                WildcardTypeName.subtypeOf(ClassName.get("java.lang", "Object")));
        ParameterizedTypeName listCompiledRuleType = ParameterizedTypeName.get(
                ClassName.get(List.class),
                ParameterizedTypeName.get(
                        ClassName.get("com.libentity.decision", "CompiledRule"),
                        valueType,
                        WildcardTypeName.subtypeOf(ClassName.get("java.lang", "Object"))));
        ParameterizedTypeName functionType = ParameterizedTypeName.get(
                ClassName.get("java.util.function", "Function"), valueType, ClassName.get("java.lang", "Object"));

        // Generate getCompileRules(Input input)
        CodeBlock.Builder compiledRulesBuilder = CodeBlock.builder()
                .add("return $T.of(\n", ClassName.get(List.class))
                .indent();
        for (int i = 0; i < fieldNames.size(); i++) {
            String name = fieldNames.get(i);
            TypeName fieldGenericType = fieldGenericTypes.get(i);
            compiledRulesBuilder.add(
                    "new $T<$T, $T>($S, input.$L.ruleEvaly, f -> input.$L.ruleEvaly.eval(f), i -> i.$L())",
                    ClassName.get("com.libentity.decision", "CompiledRule"),
                    valueType,
                    fieldGenericType,
                    name,
                    name,
                    name,
                    name);
            if (i < fieldNames.size() - 1) {
                compiledRulesBuilder.add(",\n");
            }
        }
        compiledRulesBuilder.add("\n").unindent().add(")");

        MethodSpec getCompileRules = MethodSpec.methodBuilder("getCompileRules")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(inputType, "input")
                .returns(listCompiledRuleType)
                .addStatement(compiledRulesBuilder.build())
                .build();

        // Build InputRuleProvider implementing InputProvider<Input, InputValue>
        TypeSpec classSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(interfaceType)
                .addMethod(getCompileRules)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, classSpec).build();
        javaFile.writeTo(processingEnv.getFiler());
    }

    private TypeMirror extractGenericType(TypeMirror typeMirror) {
        // Simplified: assumes typeMirror is DeclaredType (e.g., RuleIn<T>)
        if (typeMirror instanceof javax.lang.model.type.DeclaredType) {
            javax.lang.model.type.DeclaredType declaredType = (javax.lang.model.type.DeclaredType) typeMirror;
            if (declaredType.asElement().getSimpleName().toString().equals("Rule")) {
                return declaredType.getTypeArguments().get(0);
            }
        }
        return null;
    }
}
