package com.fr.plugin

import com.fr.intelli.record.Focus
import com.fr.intelli.record.Original
import com.fr.record.analyzer.EnableMetrics
import com.fr.script.AbstractFunction

@EnableMetrics
class AnotherFunc :AbstractFunction() {
    @Focus(id = "com.fr.plugin.function.test", text = "Plugin-Test_Function_Abs", source = Original.PLUGIN)
    override fun run(args: Array<out Any>?): Any {
        return "来自Kotlin"
    }
}