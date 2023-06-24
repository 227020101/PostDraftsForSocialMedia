package edu.shape.postdraftsforsocialmedia

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.shape.postdraftsforsocialmedia.Model.Contacts
import edu.shape.postdraftsforsocialmedia.Model.SqliteDatabase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SqliteDatabaseTest {

    private lateinit var dbHelper: SqliteDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dbHelper = SqliteDatabase(context)
        dbHelper.writableDatabase.execSQL("DELETE FROM ${SqliteDatabase.TABLE_CONTACTS}")
    }

    @Test
    fun testAddContacts() {
        dbHelper.addContacts()
        val contacts = dbHelper.listContacts()
        assertEquals(1, contacts.size)
        assertEquals("New Post", contacts[0].name)
    }

    @Test
    fun testUpdateContacts() {
        dbHelper.addContacts()
        val contacts = Contacts(9999,"Testing")
        dbHelper.updateContacts(contacts)
        val updatedContact = dbHelper.listContacts().last()
        assertEquals(contacts, updatedContact)
    }

    @Test
    fun testDeleteContact() {
        dbHelper.addContacts()
        val contacts = dbHelper.listContacts().last()
        dbHelper.deleteContact(9999)
        val storeContacts = dbHelper.listContacts()
        assertEquals(0, storeContacts.size)
    }
}