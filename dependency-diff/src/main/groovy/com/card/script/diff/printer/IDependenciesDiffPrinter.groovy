package com.card.script.diff.printer

import com.card.script.diff.model.DependenciesDiffModel


interface IDependenciesDiffPrinter {
    void printer(List<DependenciesDiffModel> diffModels)
}