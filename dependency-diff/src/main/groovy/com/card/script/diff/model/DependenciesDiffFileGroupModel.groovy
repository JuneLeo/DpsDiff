package com.card.script.diff.model

class DependenciesDiffFileGroupModel {
    List<DependenciesDiffFileModel> diffFileModelList = new ArrayList<>()
    long diff = 0
    int type

    def add(DependenciesDiffFileModel dependenciesDiffFileModel) {
        diff += dependenciesDiffFileModel.diffFileSize
        diffFileModelList.add(dependenciesDiffFileModel)
    }

    List<DependenciesDiffFileModel> getList() {
        return diffFileModelList
    }

    String getType() {
        return DependenciesDiffFileModel.getType(type)
    }

}
