package com.example

import com.example.data.model.PostType
import com.example.data.model.UserRole
import org.junit.Assert.*
import org.junit.Test

class CampusConnectUnitTest {

    @Test
    fun userRole_badgeColors_areValid() {
        val studentColor = UserRole.STUDENT.badgeColor
        val facultyColor = UserRole.FACULTY.badgeColor
        val adminColor = UserRole.UNIVERSITY_ADMIN.badgeColor

        assertTrue(studentColor != 0L)
        assertTrue(facultyColor != 0L)
        assertTrue(adminColor != 0L)
    }

    @Test
    fun userRole_displayNames_areCorrect() {
        assertEquals("Student", UserRole.STUDENT.displayName)
        assertEquals("Faculty", UserRole.FACULTY.displayName)
        assertEquals("Alumni", UserRole.ALUMNI.displayName)
        assertEquals("Club Leader", UserRole.CLUB_LEADER.displayName)
        assertEquals("Department Admin", UserRole.DEPARTMENT_ADMIN.displayName)
        assertEquals("University Admin", UserRole.UNIVERSITY_ADMIN.displayName)
    }

    @Test
    fun serverConfig_defaultValues_areValid() {
        val config = com.example.data.repository.ServerConfig()
        assertTrue(config.serverUrl.startsWith("https://"))
        assertTrue(config.isAutoSyncEnabled)
        assertEquals(10, config.autoSyncIntervalSec)
    }
}
