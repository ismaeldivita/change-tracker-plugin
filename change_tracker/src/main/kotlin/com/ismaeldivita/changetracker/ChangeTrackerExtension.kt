package com.ismaeldivita.changetracker

import java.io.Serializable

open class ChangeTrackerExtension : Serializable {

    var tasks = emptySet<String>()
    var whitelist = emptySet<String>()
    var blacklist = emptySet<String>()
    var reevaluate = emptySet<String>()
    var branch = "master"

}
