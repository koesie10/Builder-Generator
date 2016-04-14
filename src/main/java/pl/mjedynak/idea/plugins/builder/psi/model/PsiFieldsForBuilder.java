package pl.mjedynak.idea.plugins.builder.psi.model;

import com.google.common.collect.ImmutableList;
import com.intellij.psi.PsiField;

import java.util.List;

public class PsiFieldsForBuilder {

    private List<PsiField> psiFields;

    public PsiFieldsForBuilder(List<PsiField> psiFields) {
        this.psiFields = ImmutableList.copyOf(psiFields);
    }

    public List<PsiField> getFields() {
        return psiFields;
    }
}
