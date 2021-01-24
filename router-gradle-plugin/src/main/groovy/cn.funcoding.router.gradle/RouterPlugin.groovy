package cn.funcoding.router.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class RouterPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("进入了RouterPlugin")

        project.getExtensions().create("router", RouterExtension)


        RouterExtension extension = project["router"]

        println("router: ${extension.wikiDir}")
    }
}