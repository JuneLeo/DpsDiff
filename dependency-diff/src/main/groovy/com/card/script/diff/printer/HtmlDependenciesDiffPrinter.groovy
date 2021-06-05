package com.card.script.diff.printer

import com.card.script.Utils
import com.card.script.diff.DependenciesDiffTask
import com.card.script.diff.model.DependenciesDiffFileGroupModel
import com.card.script.diff.model.DependenciesDiffFileModel
import com.card.script.diff.model.DependenciesDiffModel
import org.gradle.api.Project

class HtmlDependenciesDiffPrinter implements IDependenciesDiffPrinter {
    Project mProject
    String buildDir

    HtmlDependenciesDiffPrinter(Project project, String dir) {
        this.mProject = project
        this.buildDir = dir
    }

    @Override
    void printer(List<DependenciesDiffModel> diffModels) {

        File reportDir = mProject.file(buildDir + File.separator + 'report')
        if (reportDir.exists()) {
            reportDir.deleteDir()
        }
        reportDir.mkdirs()

        File indexFile = mProject.file(reportDir.getAbsolutePath() + File.separator + "index.html")
        FileWriter fileWriter = new FileWriter(indexFile)
        fileWriter.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" rel=\"stylesheet\">" +
                "<title>Diff Index</title>\n" +
                "</head>")
        fileWriter.write("<body>")

        Map<Integer, List<DependenciesDiffModel>> diffModelMap = new HashMap<>()

        for (DependenciesDiffModel diffModel : diffModels) {
            def diffModelList = diffModelMap.get(diffModel.status)
            if (diffModelList == null) {
                diffModelList = new ArrayList<>()
                diffModelMap.put(diffModel.status, diffModelList)
            }
            diffModelList.add(diffModel)
        }

        diffModelMap.toSorted(new Comparator<Map.Entry<Integer, List<DependenciesDiffModel>>>() {
            @Override
            int compare(Map.Entry<Integer, List<DependenciesDiffModel>> o1, Map.Entry<Integer, List<DependenciesDiffModel>> o2) {
                return o1.key - o2.key
            }
        })


        for (def entry : diffModelMap.entrySet()) {

            fileWriter.write("<div style=\"background-color: #0ba194;width: 100%;height: 50px;vertical-align: middle\"> <p style=\"font-size: 30px;display: table-cell;height: 50px;vertical-align: middle\" >")
            fileWriter.write(DependenciesDiffModel.getNameByStatus(entry.key) + "&nbsp;")
            fileWriter.write("</p></div>")

            fileWriter.write("<table class=\"table table-striped\">")

            fileWriter.write("<tr>")

            fileWriter.write("<td>")
            fileWriter.write("包")
            fileWriter.write("</td>")

            fileWriter.write("<td>")
            fileWriter.write("新版本")
            fileWriter.write("</td>")

            fileWriter.write("<td>")
            fileWriter.write("老版本")
            fileWriter.write("</td>")

            fileWriter.write("<td>")
            fileWriter.write("差异")
            fileWriter.write("</td>")


            fileWriter.write("</tr>")

            for (DependenciesDiffModel diffModel : entry.value) {

//                if (diffModel.status == DependenciesDiffModel.STATUS_SAME_VERSION) {
//                    //没有变化的依赖
//                    continue
//                }


                String htmlPath = diffModel.module.replace(":", "_") + ".html"
                File htmlFile = mProject.file(reportDir.getAbsolutePath() + File.separator + htmlPath)
                if (htmlFile.exists()) {
                    htmlFile.delete()
                }
                Map<Integer, DependenciesDiffFileGroupModel> diffFileGroupModelMap
                if (diffModel.dependenciesDiffFileModels != null) {
                    diffFileGroupModelMap = htmlChild(htmlFile, diffModel)
                }
                fileWriter.write("<tr> \n")

                fileWriter.write("<td>\n")
                fileWriter.write(String.format("<a href=\"%s\">\n", "./" + htmlPath))
                fileWriter.write(diffModel.getModule())
                fileWriter.write("</a>\n")
                fileWriter.write(getModuleDiffFileGroup(diffFileGroupModelMap))
                fileWriter.write("</td>\n")

                fileWriter.write("<td>\n")
                fileWriter.write(diffModel.getModelInfoByHtml())
                fileWriter.write("</td>\n")

                fileWriter.write("<td>\n")
                fileWriter.write(diffModel.getOldModelInfoByHtml())
                fileWriter.write("</td>\n")

                fileWriter.write("<td>\n")
                fileWriter.write(diffModel.diff + "")
                fileWriter.write("</td>\n")

                fileWriter.write("</tr>\n")

            }
            fileWriter.write("</table>\n")
        }
        fileWriter.write("</body>\n")
        fileWriter.write("</html>\n")
        fileWriter.close()
        println("点击查看")
        println("file://" + indexFile.getAbsolutePath())
    }


    static String getModuleDiffFileGroup(Map<Integer, DependenciesDiffFileGroupModel> diffFileGroupModelMap) {
        if (diffFileGroupModelMap == null) {
            return ""
        }
        StringBuilder stringBuilder = new StringBuilder()
        for (DependenciesDiffFileGroupModel groupModel : diffFileGroupModelMap.values()) {
            if (groupModel.diff != 0) {
                stringBuilder.append("<br />")
                stringBuilder.append(Utils.getSpace('&nbsp;', 10, groupModel.getType()) + " : " + groupModel.diff)
            }
        }
        return stringBuilder.toString()
    }

    static Map<Integer, DependenciesDiffFileGroupModel> htmlChild(File htmlFile, DependenciesDiffModel dependenciesDiffModel) {

        FileWriter fileWriter = new FileWriter(htmlFile)
        fileWriter.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" rel=\"stylesheet\">" +
                "<title>" + htmlFile.getName() + "</title>\n" +
                "</head>")
        fileWriter.write("<body>")

        Map<Integer, DependenciesDiffFileGroupModel> diffFileModelMap = DependenciesDiffModel.doHandleGroup(dependenciesDiffModel.dependenciesDiffFileModels)
        for (DependenciesDiffFileGroupModel groupModel : diffFileModelMap.values()) {
            Collection<DependenciesDiffFileModel> diffFileModels = groupModel.getList()
            List<DependenciesDiffFileModel> tempList = new ArrayList<>()
            for (DependenciesDiffFileModel diffFileModel : diffFileModels) {
                if (diffFileModel.diffFileSize != 0) {
                    tempList.add(diffFileModel)
                }
            }

            if (tempList == null || tempList.isEmpty()) {
                continue
            }
            fileWriter.write("<div style=\"background-color: #0ba194;width: 100%;height: 50px;vertical-align: middle\"> <p style=\"font-size: 30px;display: table-cell;height: 50px;vertical-align: middle\" >")
            fileWriter.write(groupModel.getType() + "&nbsp;")
            fileWriter.write("</p></div>")
            fileWriter.write("<table class=\"table table-striped\">")

            fileWriter.write("<tr>")

            fileWriter.write("<td>")
            fileWriter.write("状态")
            fileWriter.write("</td>")

            fileWriter.write("<td>")
            fileWriter.write("文件")
            fileWriter.write("</td>")

            fileWriter.write("<td>")
            fileWriter.write("差异")
            fileWriter.write("</td>")

            fileWriter.write("</tr>")

            for (DependenciesDiffFileModel diffFileModel : tempList) {

                if (diffFileModel.diffFileSize != 0) {
                    //println("    " + diffFileModel.getChange() + " : " + diffFileModel.shortPath + ", " + diffFileModel.diff)

                    fileWriter.write("<tr>")

                    fileWriter.write("<td>")
                    fileWriter.write(diffFileModel.getChange())
                    fileWriter.write("</td>")

                    fileWriter.write("<td>")
                    fileWriter.write(diffFileModel.fileShortPath)
                    fileWriter.write("</td>")

                    fileWriter.write("<td>")
                    fileWriter.write(diffFileModel.diffFileSize + "")
                    fileWriter.write("</td>")

                    fileWriter.write("</tr>")
                }
            }

            fileWriter.write("</table>")

        }


        fileWriter.write("</body>")
        fileWriter.write("</html>")
        fileWriter.close()

        return diffFileModelMap
    }

}