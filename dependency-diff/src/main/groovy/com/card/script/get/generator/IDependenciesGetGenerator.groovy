package com.card.script.get.generator

import com.card.script.get.model.DependenciesGetModel

interface IDependenciesGetGenerator<T extends DependenciesGetModel> {
    List<T> generator(String classpath)
}
