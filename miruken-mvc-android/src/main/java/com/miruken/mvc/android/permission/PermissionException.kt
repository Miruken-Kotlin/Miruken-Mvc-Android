package com.miruken.mvc.android.permission

class PermissionException(val result: Permissions.Result, message: String): Exception(message)