package com.card.script.get.printer

import com.card.script.get.model.DependenciesGetModel

interface DependenciesGetPrinter<T extends DependenciesGetModel> {
    void printer(List<T> dependenciesGetModels)
}
