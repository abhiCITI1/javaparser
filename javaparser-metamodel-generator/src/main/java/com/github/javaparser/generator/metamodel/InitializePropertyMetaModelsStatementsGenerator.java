package com.github.javaparser.generator.metamodel;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.UnionType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.github.javaparser.JavaParser.parseStatement;
import static com.github.javaparser.ast.Modifier.PUBLIC;
import static com.github.javaparser.generator.metamodel.MetaModelGenerator.isNode;
import static com.github.javaparser.generator.metamodel.MetaModelGenerator.nodeMetaModelName;
import static com.github.javaparser.utils.CodeGenerationUtils.*;
import static com.github.javaparser.utils.Utils.decapitalize;

public class InitializePropertyMetaModelsStatementsGenerator {
    public void generate(Class<?> nodeClass, Field field, ClassOrInterfaceDeclaration nodeMetaModelClass, String nodeMetaModelFieldName, NodeList<Statement> initializePropertyMetaModelsStatements) throws NoSuchMethodException {

        final AstTypeAnalysis fieldAnalysis = new AstTypeAnalysis(nodeClass.getMethod(getter(field)).getGenericReturnType());

        final Class<?> fieldType = fieldAnalysis.innerType;
        final String typeName = fieldType.getTypeName().replace('$', '.');
        final String propertyMetaModelFieldName = field.getName() + "PropertyMetaModel";
        nodeMetaModelClass.addField("PropertyMetaModel", propertyMetaModelFieldName, PUBLIC);
        final String propertyInitializer = f("new PropertyMetaModel(%s, \"%s\", %s.class, %s, %s, %s, %s, %s, %s)",
                nodeMetaModelFieldName,
                field.getName(),
                typeName,
                optionalOf(decapitalize(nodeMetaModelName(fieldType)), isNode(fieldType)),
                fieldAnalysis.isOptional,
                isNonEmpty(field),
                fieldAnalysis.isNodeList,
                fieldAnalysis.isEnumSet,
                fieldAnalysis.isSelfType);
        final String fieldSetting = f("%s.%s=%s;", nodeMetaModelFieldName, propertyMetaModelFieldName, propertyInitializer);
        final String fieldAddition = f("%s.getDeclaredPropertyMetaModels().add(%s.%s);", nodeMetaModelFieldName, nodeMetaModelFieldName, propertyMetaModelFieldName);

        initializePropertyMetaModelsStatements.add(parseStatement(fieldSetting));
        initializePropertyMetaModelsStatements.add(parseStatement(fieldAddition));
    }

    public void generateDerivedProperty(Class<?> nodeClass, Method method, ClassOrInterfaceDeclaration nodeMetaModelClass, String nodeMetaModelFieldName, NodeList<Statement> initializePropertyMetaModelsStatements) throws NoSuchMethodException {

        final AstTypeAnalysis fieldAnalysis = new AstTypeAnalysis(method.getGenericReturnType());

        final Class<?> fieldType = fieldAnalysis.innerType;
        final String typeName = fieldType.getTypeName().replace('$', '.');
        final String propertyMetaModelFieldName = getterToPropertyName(method.getName()) + "PropertyMetaModel";
        nodeMetaModelClass.addField("PropertyMetaModel", propertyMetaModelFieldName, PUBLIC);
        final String propertyInitializer = f("new PropertyMetaModel(%s, \"%s\", %s.class, %s, %s, %s, %s, %s, %s)",
                nodeMetaModelFieldName,
                getterToPropertyName(method.getName()),
                typeName,
                optionalOf(decapitalize(nodeMetaModelName(fieldType)), isNode(fieldType)),
                fieldAnalysis.isOptional,
                isNonEmpty(method),
                fieldAnalysis.isNodeList,
                fieldAnalysis.isEnumSet,
                fieldAnalysis.isSelfType);
        final String fieldSetting = f("%s.%s=%s;", nodeMetaModelFieldName, propertyMetaModelFieldName, propertyInitializer);
        final String fieldAddition = f("%s.getDerivedPropertyMetaModels().add(%s.%s);", nodeMetaModelFieldName, nodeMetaModelFieldName, propertyMetaModelFieldName);

        initializePropertyMetaModelsStatements.add(parseStatement(fieldSetting));
        initializePropertyMetaModelsStatements.add(parseStatement(fieldAddition));
    }

    private boolean isNonEmpty(Field field) {
        final String name = field.getName();
        final Class<?> c = field.getDeclaringClass();
        return (c == VariableDeclarator.class && name.equals("initializer")) ||
                (c == MethodReferenceExpr.class && name.equals("identifier")) ||
                (c == Name.class && name.equals("identifier")) ||
                (c == SimpleName.class && name.equals("identifier")) ||
                (c == ArrayCreationExpr.class && name.equals("levels")) ||
                (c == FieldDeclaration.class && name.equals("variables")) ||
                (c == IntersectionType.class && name.equals("elements")) ||
                (c == UnionType.class && name.equals("elements")) ||
                (c == VariableDeclarationExpr.class && name.equals("variables"));
    }

    private boolean isNonEmpty(Method method) {
        return true;
    }

    private String getter(Field field) {
        return getterName(field.getType(), field.getName());
    }

}
