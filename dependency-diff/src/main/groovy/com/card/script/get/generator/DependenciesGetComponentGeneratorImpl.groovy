package com.card.script.get.generator

import com.card.script.get.model.DependenciesGetModel
import com.card.script.get.DependenciesGetUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedComponentResult


class DependenciesGetComponentGeneratorImpl implements IDependenciesGetGenerator<DependenciesGetModel> {

    Project project
    String variantName

    DependenciesGetComponentGeneratorImpl(Project project, String variantName) {
        this.project = project
        this.variantName = variantName
    }

    @Override
    List<DependenciesGetModel> generator(String classpath) {

        Set<ResolvedComponentResult> dependenciesResult
        Set<ResolvedArtifactResult> dependenciesAArResult


        Configuration compileConfiguration = DependenciesGetUtils.getCompileConfiguration(project, variantName)
        ArtifactCollection compileArtifactCollection = DependenciesGetUtils.getArtifactCollection(compileConfiguration)
        Set<ResolvedArtifactResult> compileResolvedArtifactResults = compileArtifactCollection.getArtifacts()
        Set<ResolvedComponentResult> compileResolvedComponentResults = compileConfiguration.getIncoming().getResolutionResult().getAllComponents()

        Configuration runtimeConfiguration = DependenciesGetUtils.getRuntimeConfiguration(project, variantName)
        ArtifactCollection runtimeArtifactCollection = DependenciesGetUtils.getArtifactCollection(runtimeConfiguration)
        Set<ResolvedArtifactResult> runtimeResolvedArtifactResults = runtimeArtifactCollection.getArtifacts()
        Set<ResolvedComponentResult> runtimeResolvedComponentResults = runtimeConfiguration.getIncoming().getResolutionResult().getAllComponents()


        if (classpath == 'compile') {
            dependenciesAArResult = compileResolvedArtifactResults
            dependenciesResult = compileResolvedComponentResults
        } else if (classpath == 'all') {
            dependenciesAArResult = DependenciesGetUtils.composeResolvedArtifactResult(compileResolvedArtifactResults, runtimeResolvedArtifactResults)
            dependenciesResult = DependenciesGetUtils.composeResolvedComponentResult(compileResolvedComponentResults, runtimeResolvedComponentResults)
        } else {
            dependenciesAArResult = runtimeResolvedArtifactResults
            dependenciesResult = runtimeResolvedComponentResults
        }

        List<DependenciesGetModel> dependenciesGetModelLists = new ArrayList<>()

        for (ResolvedComponentResult dependencyResult : dependenciesResult) {
            String version = dependencyResult.getModuleVersion().getVersion()
            if (version == 'unspecified') {
                continue
            }
            String moduleAndVersion = dependencyResult.getModuleVersion().toString();
            String module = dependencyResult.getModuleVersion().getModule().toString()
            String group = dependencyResult.getModuleVersion().getModule().getGroup()
            String artifact = dependencyResult.getModuleVersion().getModule().getName()

            ResolvedArtifactResult resolvedArtifactResult = getArtifactResult(dependenciesAArResult, moduleAndVersion)
            if (resolvedArtifactResult == null) {
                throw new RuntimeException("not find ResolvedArtifactResult")
            }


            DependenciesGetModel dependenciesGetModel = new DependenciesGetModel()
            dependenciesGetModel.module = moduleAndVersion
            dependenciesGetModel.group = group
            dependenciesGetModel.artifact = artifact
            dependenciesGetModel.version = version
            dependenciesGetModel.path = resolvedArtifactResult.getFile().getAbsolutePath()

            dependenciesGetModelLists.add(dependenciesGetModel)
        }

        return dependenciesGetModelLists
    }

    ResolvedArtifactResult getArtifactResult(Set<ResolvedArtifactResult> resolvedArtifactResults, String module) {
        for (ResolvedArtifactResult result : resolvedArtifactResults) {
            ComponentIdentifier identifier = result.getId().getComponentIdentifier()
            if (identifier.getDisplayName() == module) {
                return result
            }
        }
        return null
    }

}
