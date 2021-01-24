package cn.funcoding.router.gradle

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("========RouterPlugin Start========")
        // 1.自动帮助用户传递路径参数到注解处理器中
        if (project.extensions.findByName("kapt") != null) {
            project.extensions.findByName("kapt").arguments {
                arg("root_project_dir", project.rootProject.projectDir.absolutePath)
            }
        }
        // 2.实现久的构建产物的自动清理
        project.clean.doFirst {
            File routerMappingDir = new File(project.rootProject.projectDir, "router_mapping")
            if (routerMappingDir.exists()) {
                routerMappingDir.deleteDir()
            }
        }

        project.getExtensions().create("router", RouterExtension)
        project.afterEvaluate {
            RouterExtension extension = project["router"]
            def wikiDir = extension.wikiDir
            println("wikiDir: ${wikiDir}")

            // 3. 在javac任务(compileDebugJavaWithJavac)后，汇总生成文档
            project.tasks.findAll { task ->
                task.name.startsWith('compile') && task.name.endsWith('JavaWithJavac')
            }.each { task ->
                task.doLast {
                    File routerMappingDir = new File(project.rootProject.projectDir, "router_mapping")
                    if (!routerMappingDir.exists()) {
                        return
                    }

                    File[] allChildFiles = routerMappingDir.listFiles()
                    if (allChildFiles.length < 1) {
                        return
                    }

                    StringBuilder markdownBuilder = new StringBuilder()
                    markdownBuilder.append("# 页面文档\n\n")
                    allChildFiles.each { child ->
                        if (child.name.endsWith(".json")) {
                            JsonSlurper jsonSlurper = new JsonSlurper()
                            def content = jsonSlurper.parse(child)
                            content.each { innerContent ->
                                def url = innerContent["url"]
                                def description = innerContent["description"]
                                def realPath = innerContent["realPath"]

                                markdownBuilder.append("## $description \n")
                                markdownBuilder.append("- url: $url \n")
                                markdownBuilder.append("- realPath: $realPath \n")
                            }
                        }
                    }

                    File wikiFileDir = new File(wikiDir)
                    if (!wikiFileDir.exists()) {
                        wikiFileDir.mkdir()
                    }
                    File wikiFile = new File(wikiFileDir, "页面文档.md")
                    if (wikiFile.exists()) {
                        wikiFile.delete()
                    }
                    wikiFile.write(markdownBuilder.toString())
                }
            }
        }
    }
}