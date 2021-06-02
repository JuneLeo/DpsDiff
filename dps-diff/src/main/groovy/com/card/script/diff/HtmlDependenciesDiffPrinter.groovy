package com.card.script.diff

import com.card.script.Utils
import org.gradle.api.Project

class HtmlDependenciesDiffPrinter implements IDependenciesDiffPrinter {
    Project mProject

    HtmlDependenciesDiffPrinter(Project project) {
        this.mProject = project
    }

    @Override
    void printer(List<DependenciesDiffTask.DependenciesDiffModel> diffModels) {

        File buildDir = mProject.file(mProject.buildDir.getAbsolutePath() + File.separator + DependenciesDiffTask.BUILD_DIR)

        File reportDir = mProject.file(buildDir.getAbsolutePath() + File.separator + 'report')
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

        for (DependenciesDiffTask.DependenciesDiffModel diffModel : diffModels) {
            fileWriter.write("<a href=\"http://www.w3school.com.cn\">")
            String htmlPath = diffModel.module.replace(":", "_") + ".html"
            File htmlFile = mProject.file(reportDir.getAbsolutePath() + File.separator + htmlPath)
            if (htmlFile.exists()) {
                htmlFile.delete()
            }

            if (diffModel.dependenciesDiffFileModels != null) {
                htmlChild(htmlFile, diffModel.dependenciesDiffFileModels)
            }
            fileWriter.write("<tr> \n")

            fileWriter.write("<td>\n")
            fileWriter.write(String.format("<a href=\"%s\">\n", "./" + htmlPath))
            fileWriter.write(diffModel.getModule())
            fileWriter.write("</a>\n")
            fileWriter.write(getModuleDiffFileGroup(diffModel))
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

        fileWriter.write("</body>\n")
        fileWriter.write("</html>\n")
        fileWriter.close()
        println("点击查看")
        println("file://" + indexFile.getAbsolutePath())
    }


    String getModuleDiffFileGroup(DependenciesDiffTask.DependenciesDiffModel diffModel) {
        StringBuilder stringBuilder = new StringBuilder()
        for (DependenciesDiffTask.DependenciesDiffFileGroupModel groupModel : diffModel.diffFileModelMap.values()) {
            stringBuilder.append("<br />")
            stringBuilder.append(Utils.getSpace('&nbsp;',10,groupModel.getType()) + " : " + groupModel.diff)
        }
        return stringBuilder.toString()
    }

    def htmlChild(File htmlFile, List<DependenciesDiffTask.DependenciesDiffFileModel> diffFileModels) {

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

        for (DependenciesDiffTask.DependenciesDiffFileModel diffFileModel : diffFileModels) {

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

        fileWriter.write("</body>")
        fileWriter.write("</html>")
        fileWriter.close()
    }

}