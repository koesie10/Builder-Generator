package pl.mjedynak.idea.plugins.builder.psi;

import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;
import pl.mjedynak.idea.plugins.builder.writer.BuilderContext;

import java.util.List;

public class BuilderPsiClassBuilder {

    private static final String PRIVATE_STRING = "private";
    static final String STATIC_MODIFIER = "static";

    private PsiHelper psiHelper = new PsiHelper();
    private PsiFieldsModifier psiFieldsModifier = new PsiFieldsModifier();
    private MethodCreator methodCreator;

    private PsiClass srcClass = null;
    private String builderClassName = null;

    private List<PsiField> psiFields = null;

    private PsiClass builderClass = null;
    private PsiElementFactory elementFactory = null;
    private String srcClassName = null;
    private String srcClassFieldName = null;

    public BuilderPsiClassBuilder aBuilder(BuilderContext context) {
        initializeFields(context);
        JavaDirectoryService javaDirectoryService = psiHelper.getJavaDirectoryService();
        builderClass = javaDirectoryService.createClass(context.getTargetDirectory(), builderClassName);
        return this;
    }

    public BuilderPsiClassBuilder anInnerBuilder(BuilderContext context) {
        initializeFields(context);
        builderClass = elementFactory.createClass(builderClassName);
        builderClass.getModifierList().setModifierProperty(STATIC_MODIFIER, true);
        return this;
    }

    private void initializeFields(BuilderContext context) {
        JavaPsiFacade javaPsiFacade = psiHelper.getJavaPsiFacade(context.getProject());
        elementFactory = javaPsiFacade.getElementFactory();
        srcClass = context.getPsiClassFromEditor();
        builderClassName = context.getClassName();
        srcClassName = context.getPsiClassFromEditor().getName();
        srcClassFieldName = StringUtils.uncapitalize(srcClassName);
        psiFields = context.getPsiFieldsForBuilder().getFields();
        methodCreator = new MethodCreator(elementFactory, builderClassName);
    }

    public BuilderPsiClassBuilder withFields() {
        psiFieldsModifier.modifyFields(psiFields, builderClass);
        return this;
    }

    public BuilderPsiClassBuilder withPrivateConstructor() {
        PsiMethod constructor = elementFactory.createConstructor();
        constructor.getModifierList().setModifierProperty(PRIVATE_STRING, true);
        builderClass.add(constructor);
        return this;
    }

    public BuilderPsiClassBuilder withInitializingMethod() {
        PsiMethod staticMethod = elementFactory.createMethodFromText("public static " + builderClassName + " builder() { return new " + builderClassName + "();}", srcClass);
        srcClass.add(staticMethod);
        return this;
    }

    public BuilderPsiClassBuilder withConstructorMethod() {
        StringBuilder constructor = new StringBuilder();
        constructor.append("public ").append(srcClassName).append("(").append(builderClassName).append(" builder) {");
        for (PsiField field : psiFields) {
            constructor.append("this.").append(field.getName()).append(" = ").append("builder.").append(field.getName()).append(";");
        }
        constructor.append("}");
        PsiMethod constructorMethod = elementFactory.createMethodFromText(constructor.toString(), srcClass);
        srcClass.add(constructorMethod);
        return this;
    }

    public BuilderPsiClassBuilder withSetMethods(String methodPrefix) {
        for (PsiField psiFieldForConstructor : psiFields) {
            createAndAddMethod(psiFieldForConstructor, methodPrefix);
        }
        return this;
    }

    private void createAndAddMethod(PsiField psiField, String methodPrefix) {
        builderClass.add(methodCreator.createMethod(psiField, methodPrefix));
    }

    public PsiClass build() {
        PsiMethod buildMethod = elementFactory.createMethodFromText("public " + srcClassName + " build() { " + "return new " + srcClassName + "(this);}", srcClass);
        builderClass.add(buildMethod);
        return builderClass;
    }
}
