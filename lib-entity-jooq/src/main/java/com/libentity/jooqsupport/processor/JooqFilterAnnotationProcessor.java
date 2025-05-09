package com.libentity.jooqsupport.processor;

import com.libentity.jooqsupport.annotation.*;
import com.squareup.javapoet.*;
import java.io.IOException;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({
    "com.libentity.jooqsupport.annotation.JooqFilter",
    "com.libentity.jooqsupport.annotation.JooqFilterField"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class JooqFilterAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(JooqFilter.class)) {
            if (element.getKind() != ElementKind.CLASS) continue;
            TypeElement filterClass = (TypeElement) element;
            String filterClassName = filterClass.getSimpleName().toString();
            String packageName = processingEnv
                    .getElementUtils()
                    .getPackageOf(filterClass)
                    .getQualifiedName()
                    .toString();
            String metaClassName = filterClassName + "JooqMeta";

            // Gather filter fields
            List<VariableElement> fields = ElementFilter.fieldsIn(filterClass.getEnclosedElements());
            List<FieldSpec> metaFields = new ArrayList<>();
            CodeBlock.Builder conditionBuilder = CodeBlock.builder();
            conditionBuilder.addStatement("org.jooq.Condition condition = org.jooq.impl.DSL.trueCondition()");

            // Define table class for static field references
            JooqFilter jooqFilterAnnotation = filterClass.getAnnotation(JooqFilter.class);
            String jooqTableClass = jooqFilterAnnotation.tableClass();
            String jooqTableVar = jooqFilterAnnotation.tableVar();

            // --- Virtual Field Support ---
            // Collect virtual fields and their types
            List<VariableElement> virtualFields = new ArrayList<>();
            for (VariableElement field : fields) {
                JooqFilterField filterField = field.getAnnotation(JooqFilterField.class);
                if (filterField != null && filterField.virtual()) {
                    virtualFields.add(field);
                }
            }

            // --- Condition Logic for Non-Virtual Fields ---
            for (VariableElement field : fields) {
                JooqFilterField filterField = field.getAnnotation(JooqFilterField.class);
                if (filterField == null) continue;
                if (filterField.virtual()) {
                    // Skip virtual fields in default toCondition logic
                    continue;
                }
                String fieldName = field.getSimpleName().toString();
                String jooqFieldName = filterField.field();
                // Ensure uppercase for JOOQ static field
                String jooqFieldConst = toUpperSnakeCase(jooqFieldName);
                String fieldConst = fieldName.toUpperCase() + "_FIELD";
                metaFields.add(FieldSpec.builder(String.class, fieldConst)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", jooqFieldName)
                        .build());

                // Use JOOQ static field reference
                String jooqFieldRef = jooqTableClass + "." + jooqTableVar + "." + jooqFieldConst;

                boolean isChronoLocalDate = field.asType().toString().contains("ChronoLocalDate");
                String valueCast = isChronoLocalDate ? ".query(java.time.LocalDate::from)" : "";

                if (field.asType().toString().contains("RangeFilter")) {
                    // gt (GT comparator)
                    conditionBuilder.beginControlFlow(
                            "if (filter.get$L() != null && filter.get$L().getGt() != null)",
                            capitalize(fieldName),
                            capitalize(fieldName));
                    conditionBuilder.addStatement(
                            "condition = condition.and($L.gt(filter.get$L().getGt()$L))",
                            jooqFieldRef,
                            capitalize(fieldName),
                            valueCast);
                    conditionBuilder.endControlFlow();
                    // gte (GTE comparator)
                    conditionBuilder.beginControlFlow(
                            "if (filter.get$L() != null && filter.get$L().getGte() != null)",
                            capitalize(fieldName),
                            capitalize(fieldName));
                    conditionBuilder.addStatement(
                            "condition = condition.and($L.ge(filter.get$L().getGte()$L))",
                            jooqFieldRef,
                            capitalize(fieldName),
                            valueCast);
                    conditionBuilder.endControlFlow();
                    // lt (LT comparator)
                    conditionBuilder.beginControlFlow(
                            "if (filter.get$L() != null && filter.get$L().getLt() != null)",
                            capitalize(fieldName),
                            capitalize(fieldName));
                    conditionBuilder.addStatement(
                            "condition = condition.and($L.lt(filter.get$L().getLt()$L))",
                            jooqFieldRef,
                            capitalize(fieldName),
                            valueCast);
                    conditionBuilder.endControlFlow();
                    // lte (LTE comparator)
                    conditionBuilder.beginControlFlow(
                            "if (filter.get$L() != null && filter.get$L().getLte() != null)",
                            capitalize(fieldName),
                            capitalize(fieldName));
                    conditionBuilder.addStatement(
                            "condition = condition.and($L.le(filter.get$L().getLte()$L))",
                            jooqFieldRef,
                            capitalize(fieldName),
                            valueCast);
                    conditionBuilder.endControlFlow();
                    // eq (EQ comparator)
                    conditionBuilder.beginControlFlow(
                            "if (filter.get$L() != null && filter.get$L().getEq() != null)",
                            capitalize(fieldName),
                            capitalize(fieldName));
                    conditionBuilder.addStatement(
                            "condition = condition.and($L.eq(filter.get$L().getEq()$L))",
                            jooqFieldRef,
                            capitalize(fieldName),
                            valueCast);
                    conditionBuilder.endControlFlow();
                } else if (field.asType().toString().equals("java.util.Set<java.lang.String>")) {
                    conditionBuilder.beginControlFlow(
                            "if (filter.get$L() != null && !filter.get$L().isEmpty())",
                            capitalize(fieldName),
                            capitalize(fieldName));
                    conditionBuilder.addStatement(
                            "condition = condition.and($L.in(filter.get$L()))", jooqFieldRef, capitalize(fieldName));
                    conditionBuilder.endControlFlow();
                } else if (field.asType().toString().equals("java.lang.Boolean")) {
                    conditionBuilder.beginControlFlow("if (filter.get$L() != null)", capitalize(fieldName));
                    conditionBuilder.addStatement(
                            "condition = condition.and($L.eq(filter.get$L()))", jooqFieldRef, capitalize(fieldName));
                    conditionBuilder.endControlFlow();
                }
            }

            // --- Virtual Field Logic ---
            if (!virtualFields.isEmpty()) {
                conditionBuilder.addStatement("// Virtual field logic");
                conditionBuilder.addStatement("if (factory != null)");
                conditionBuilder.beginControlFlow("");
                for (VariableElement vField : virtualFields) {
                    JooqFilterField filterField = vField.getAnnotation(JooqFilterField.class);
                    String vFieldName = vField.getSimpleName().toString();
                    String methodName = "get" + capitalize(vFieldName) + "Mapper";
                    String comparatorsList = "java.util.List.of("
                            + Arrays.stream(filterField.comparators())
                                    .map(Enum::name)
                                    .map(comparator -> "com.libentity.jooqsupport.annotation.Comparator." + comparator)
                                    .collect(java.util.stream.Collectors.joining(", "))
                            + ")";
                    conditionBuilder.beginControlFlow("if (filter.get$L() != null)", capitalize(vFieldName));
                    conditionBuilder.addStatement("var mapper = factory.$L($L)", methodName, comparatorsList);
                    conditionBuilder.beginControlFlow("if (mapper != null)");
                    conditionBuilder.addStatement("org.jooq.Condition virtualCond = mapper.map(filter)");
                    conditionBuilder.beginControlFlow("if (virtualCond != null)");
                    conditionBuilder.addStatement("condition = condition.and(virtualCond)");
                    conditionBuilder.endControlFlow();
                    conditionBuilder.endControlFlow();
                    conditionBuilder.endControlFlow();
                }
                conditionBuilder.endControlFlow();
            }

            conditionBuilder.addStatement("return condition");

            // Factory interface for virtual mappers
            TypeSpec.Builder factoryBuilder = TypeSpec.interfaceBuilder(metaClassName + "VirtualMapperFactory")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
            for (VariableElement vField : virtualFields) {
                JooqFilterField filterField = vField.getAnnotation(JooqFilterField.class);
                String vFieldName = vField.getSimpleName().toString();
                String methodName = "get" + capitalize(vFieldName) + "Mapper";
                TypeName mapperType = ParameterizedTypeName.get(
                        ClassName.get("com.libentity.jooqsupport", "VirtualConditionMapper"),
                        ClassName.get(packageName, filterClassName));
                ParameterizedTypeName comparatorListType = ParameterizedTypeName.get(
                        ClassName.get(List.class), ClassName.get("com.libentity.jooqsupport.annotation", "Comparator"));
                MethodSpec defaultMethod = MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .returns(mapperType)
                        .addParameter(comparatorListType, "comparators")
                        .addStatement(
                                "throw new UnsupportedOperationException(\"No mapper provided for $L\")", vFieldName)
                        .build();
                factoryBuilder.addMethod(defaultMethod);
            }

            // --- Build toCondition method, with or without VirtualMapperFactory
            MethodSpec toCondition;
            if (!virtualFields.isEmpty()) {
                toCondition = MethodSpec.methodBuilder("toCondition")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(ClassName.get("org.jooq", "Condition"))
                        .addParameter(ClassName.get(packageName, filterClassName), "filter")
                        .addParameter(
                                ClassName.get(packageName, metaClassName, metaClassName + "VirtualMapperFactory"),
                                "factory")
                        .addCode(conditionBuilder.build())
                        .build();
            } else {
                toCondition = MethodSpec.methodBuilder("toCondition")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(ClassName.get("org.jooq", "Condition"))
                        .addParameter(ClassName.get(packageName, filterClassName), "filter")
                        .addCode(conditionBuilder.build())
                        .build();
            }

            // Sorting: only default for now
            JooqFilter jooqFilterAnn = filterClass.getAnnotation(JooqFilter.class);
            String defaultSortField = null;
            String defaultSortDir = null;
            if (jooqFilterAnn != null) {
                defaultSortField = jooqFilterAnn.defaultSort().field();
                defaultSortDir = jooqFilterAnn.defaultSort().direction().toString();
            }
            CodeBlock.Builder sortBuilder = CodeBlock.builder();
            if (defaultSortField != null && defaultSortDir != null) {
                String sortFieldConst = toUpperSnakeCase(defaultSortField);
                // Fix: only append field if not empty to avoid double dot
                String sortFieldRef = jooqTableClass + "." + jooqTableVar;
                if (sortFieldConst != null && !sortFieldConst.isEmpty()) {
                    sortFieldRef += "." + sortFieldConst;
                }
                sortBuilder.addStatement(
                        "return java.util.List.of($L.$L())",
                        sortFieldRef,
                        defaultSortDir.equals("DESC") ? "desc" : "asc");
            } else {
                sortBuilder.addStatement("return java.util.List.of()");
            }
            MethodSpec getSortFields = MethodSpec.methodBuilder("getSortFields")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ParameterizedTypeName.get(
                            ClassName.get(List.class), ClassName.get("org.jooq", "SortField")))
                    .addParameter(ClassName.get(packageName, filterClassName), "filter")
                    .addCode(sortBuilder.build())
                    .build();

            TypeSpec.Builder metaClassBuilder = TypeSpec.classBuilder(metaClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addFields(metaFields)
                    .addMethod(toCondition)
                    .addMethod(getSortFields);
            if (!virtualFields.isEmpty()) {
                metaClassBuilder.addType(factoryBuilder.build());
            }
            TypeSpec metaClass = metaClassBuilder.build();

            // Remove addImport (not supported by JavaPoet), rely on fully qualified name
            JavaFile javaFile = JavaFile.builder(packageName, metaClass).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv
                        .getMessager()
                        .printMessage(Diagnostic.Kind.ERROR, "Failed to write meta: " + e.getMessage());
            }
        }
        return true;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // Converts camelCase or lower_snake_case to UPPER_SNAKE_CASE
    private static String toUpperSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").replaceAll("-", "_").toUpperCase();
    }
}
