package com.anod.appwatcher.database

import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppListTableQueriesTest {

    @Test
    fun `list query uses indexed changelog join without per row lookup`() {
        val (sql, args) = AppListTable.Queries.createAppsListQuery(
            sortId = Preferences.SORT_NAME_ASC,
            orderByRecentlyDiscovered = false,
            tagId = null,
            titleFilter = "",
            offset = SqlOffset(offset = 0, limit = 20)
        )

        assertTrue(sql.contains("LEFT JOIN changelog ON app_list.app_id == changelog.app_id"))
        assertTrue(sql.contains("AND app_list.ver_num == changelog.code"))
        assertFalse(sql.contains("SELECT MAX("))
        assertEquals(listOf(App.STATUS_DELETED.toString(), "20", "0"), args.toList())
    }

    @Test
    fun `tagged list query uses exists so duplicate app tag rows cannot duplicate app rows`() {
        val tagId = 42
        val (sql, args) = AppListTable.Queries.createAppsListQuery(
            sortId = Preferences.SORT_NAME_ASC,
            orderByRecentlyDiscovered = false,
            tagId = tagId,
            titleFilter = "",
            offset = null
        )

        assertFalse(sql.contains("INNER JOIN app_tags"))
        assertTrue(sql.contains("EXISTS (SELECT 1 FROM app_tags WHERE app_tags.app_id = app_list.app_id AND app_tags.tags_id = ?)"))
        assertEquals(listOf(App.STATUS_DELETED.toString(), tagId.toString()), args.toList())
    }

    @Test
    fun `untagged list query uses not exists`() {
        val (sql, args) = AppListTable.Queries.createAppsListQuery(
            sortId = Preferences.SORT_NAME_ASC,
            orderByRecentlyDiscovered = false,
            tagId = Tag.empty.id,
            titleFilter = "",
            offset = null
        )

        assertFalse(sql.contains("LEFT JOIN app_tags"))
        assertTrue(sql.contains("NOT EXISTS (SELECT 1 FROM app_tags WHERE app_tags.app_id = app_list.app_id)"))
        assertEquals(listOf(App.STATUS_DELETED.toString()), args.toList())
    }
}