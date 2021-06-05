package com.card.script.get

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.attributes.AttributeContainer


class DependenciesGetUtils {

    static Configuration getCompileConfiguration(Project project, String variantName) {
        return project.getConfigurations().getByName(variantName + "CompileClasspath")
    }


    static Configuration getRuntimeConfiguration(Project project, String variantName) {
        return project.getConfigurations().getByName(variantName + "RuntimeClasspath")
    }


    static ArtifactCollection getArtifactCollection(Configuration configuration) {
        Action<AttributeContainer> attributes = new Action<AttributeContainer>() {
            @Override
            void execute(AttributeContainer container) {
                //key : AndroidArtifacts.MODULE_PATH,AndroidArtifacts.ARTIFACT_TYPE
                //AndroidArtifacts中type类型 aar,android-classes等，会帮我们过滤
                // 配置有疑问 参考 VariantScopeImpl 中
                //container.attribute(ARTIFACT_TYPE, "android-classes");
            }
        }

        return configuration.getIncoming()
                .artifactView(new Action<org.gradle.api.artifacts.ArtifactView.ViewConfiguration>() {
                    @Override
                    void execute(org.gradle.api.artifacts.ArtifactView.ViewConfiguration viewConfiguration) {
                        viewConfiguration.lenient(true)
                        viewConfiguration.attributes(attributes)
                    }
                }).getArtifacts();
    }



    static Set<ResolvedArtifactResult>  composeResolvedArtifactResult(Set<ResolvedArtifactResult> firstResolvedArtifactResults,Set<ResolvedArtifactResult> secondResolvedArtifactResults){

        for (ResolvedArtifactResult runtimeResult : firstResolvedArtifactResults) {
            boolean isHas = false
            for (ResolvedArtifactResult result : secondResolvedArtifactResults) {
                if (runtimeResult.getId().getDisplayName() == result.getId().getDisplayName()) {
                    isHas = true
                    break
                }
            }
            if (!isHas) {
                secondResolvedArtifactResults.add(runtimeResult)
            }
        }
        return secondResolvedArtifactResults
    }



    static Set<ResolvedArtifactResult>  composeResolvedComponentResult(Set<ResolvedComponentResult> firstResolvedComponentResults, Set<ResolvedComponentResult> secondResolvedComponentResults){
        for (ResolvedComponentResult runtimeResult : firstResolvedComponentResults) {
            boolean isHas = false
            for (ResolvedComponentResult result : secondResolvedComponentResults) {
                if (result.getModuleVersion().getModule().getGroup() == runtimeResult.getModuleVersion().getModule().getGroup() &&
                        result.getModuleVersion().getModule().getName() == runtimeResult.getModuleVersion().getModule().getName()) {
                    isHas = true
                    break
                }
            }
            if (!isHas) {
                secondResolvedComponentResults.add(runtimeResult)
            }
        }
        return secondResolvedComponentResults
    }

}
