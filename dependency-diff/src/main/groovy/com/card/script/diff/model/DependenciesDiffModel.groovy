package com.card.script.diff.model

import com.card.script.Utils
import com.card.script.diff.DependenciesDiffTask
import org.jetbrains.annotations.NotNull

class DependenciesDiffModel implements Comparable<DependenciesDiffModel> {
    public static final STATUS_NORMAL = 1
    public static final STATUS_CHANGE = 2
    public static final STATUS_NEW = 3
    public static final STATUS_DELETE = 4
    public static final STATUS_SAME_VERSION = 5
    public static final STATUS_SAME_CONTENT = 6


    public int status = STATUS_NORMAL
    public List<DependenciesDiffFileModel> dependenciesDiffFileModels = new ArrayList<>()
    public DependenciesModel firstDependenciesModel
    public DependenciesModel secondDependenciesModel


    public long diff

    static String getNameByStatus(int status) {
        if (status == STATUS_CHANGE) {
            return '改变'
        } else if (status == STATUS_NEW) {
            return '新增'
        } else if (status == STATUS_DELETE) {
            return '删除'
        } else if (status == STATUS_SAME_VERSION) {
            return '版本未改变'
        } else if (status == STATUS_SAME_CONTENT) {
            return '内容没有改变'
        } else {
            return '未知'
        }
    }

    def handle() {
        if (firstDependenciesModel != null && secondDependenciesModel != null) {
            diff = firstDependenciesModel.size - secondDependenciesModel.size
        } else if (firstDependenciesModel != null) {
            diff = firstDependenciesModel.size
        } else if (secondDependenciesModel != null) {
            diff = -secondDependenciesModel.size
        }
        doHandleStatus()
    }


    def doHandleStatus() {
        if (status == STATUS_CHANGE) {
            boolean isChange = false
            for (def diffModel : dependenciesDiffFileModels) {
                if (diffModel.fileChange != DependenciesDiffFileModel.CHANGE_EQ) {
                    isChange = true
                    break
                }
            }
            if (!isChange) {
                status = STATUS_SAME_CONTENT
            }
        }
    }

    static Map<Integer, DependenciesDiffFileGroupModel> doHandleGroup(List<DependenciesDiffFileModel> dependenciesDiffFileModelLists) {
        Map<Integer, DependenciesDiffFileGroupModel> diffFileModelMap = new HashMap<>()
        for (DependenciesDiffFileModel dependenciesDiffFileModel : dependenciesDiffFileModelLists) {
            int fileType = dependenciesDiffFileModel.fileType
            DependenciesDiffFileGroupModel groupModel = diffFileModelMap.get(fileType)
            if (groupModel == null) {
                groupModel = new DependenciesDiffFileGroupModel()
                groupModel.type = fileType
                diffFileModelMap.put(fileType, groupModel)
            }
            groupModel.add(dependenciesDiffFileModel)
        }
        return diffFileModelMap
    }

    long getModelSize() {
        if (firstDependenciesModel != null) {
            return firstDependenciesModel.size
        }
        return 0
    }

    long getOldModelSize() {
        if (firstDependenciesModel != null) {
            return firstDependenciesModel.size
        }
        return 0
    }

    String getModule() {
        if (firstDependenciesModel != null) {
            return firstDependenciesModel.group + ":" + firstDependenciesModel.artifact
        } else if (secondDependenciesModel != null) {
            return secondDependenciesModel.group + ":" + secondDependenciesModel.artifact
        } else {
            return "unknow"
        }
    }

    String getModelInfo() {
        if (firstDependenciesModel != null) {
            return Utils.getSpace(30, "version : " + firstDependenciesModel.version) + ", size " + firstDependenciesModel.size
        }
        return "";
    }

    String getModelInfoByHtml() {
        if (firstDependenciesModel != null) {
            return "version : " + firstDependenciesModel.version + "<br />size : " + firstDependenciesModel.size
        }
        return "";
    }

    String getOldModelInfo() {
        if (secondDependenciesModel != null) {
            return Utils.getSpace(30, "version : " + secondDependenciesModel.version) + ", size " + secondDependenciesModel.size
        }
        return "";
    }

    String getOldModelInfoByHtml() {
        if (secondDependenciesModel != null) {
            return "version : " + secondDependenciesModel.version + "<br />size : " + secondDependenciesModel.size
        }
        return "";
    }


    @Override
    int compareTo(@NotNull DependenciesDiffModel o) {
        return Long.compare(o.diff, diff)
    }
}
