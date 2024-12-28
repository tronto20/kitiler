package dev.tronto.kitiler.buildsrc.tasks

import org.gradle.api.tasks.Exec
import java.io.File

abstract class PathExec : Exec() {
    private val pathList = System.getenv("PATH").split(":").map(::File)
    private fun getExecutableAbsolutePath(executable: String): String? {
        pathList.forEach {
            val maybeExecutable = it.resolve(executable)
            if (maybeExecutable.canExecute()) {
                return maybeExecutable.absolutePath
            }
        }
        return null
    }

    override fun setExecutable(executable: String?) {
        val absolutePath = executable?.let { getExecutableAbsolutePath(it) } ?: return super.setExecutable(executable)
        return super.setExecutable(absolutePath as String?)
    }

    override fun setExecutable(executable: Any) {
        return if (executable is String) setExecutable(executable as String?)
        else super.setExecutable(executable)
    }

    override fun executable(executable: Any): Exec {
        return if (executable is String) {
            getExecutableAbsolutePath(executable)?.let { super.executable(it) } ?: super.executable(executable)
        } else super.executable(executable)
    }

    private fun transformCommandLine(
        args: List<Any?>
    ): List<Any?> {
        if (args.isEmpty()) return args
        val transformedArgs = args.toMutableList()
        val executable = transformedArgs[0]
        if (executable is String) {
            getExecutableAbsolutePath(executable)?.let {
                transformedArgs[0] = it
            }
        }
        return transformedArgs
    }

    override fun commandLine(vararg arguments: Any?): Exec {
        val args = transformCommandLine(arguments.toList())
        return super.commandLine(*args.toTypedArray())
    }

    override fun commandLine(args: Iterable<*>): Exec {
        return super.commandLine(transformCommandLine(args.toList()))
    }

    override fun setCommandLine(args: List<String?>) {
        return super.setCommandLine(transformCommandLine(args.toList()))
    }

    override fun setCommandLine(args: Iterable<*>) {
        return super.setCommandLine(transformCommandLine(args.toList()))
    }

    override fun setCommandLine(vararg args: Any?) {
        return super.setCommandLine(*transformCommandLine(args.toList()).toTypedArray())
    }

}
