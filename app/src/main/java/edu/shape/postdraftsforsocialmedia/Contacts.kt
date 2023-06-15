package edu.shape.postdraftsforsocialmedia

class Contacts {
    var id = 0
    var name: String
    internal constructor(name: String) {
        this.name = name
    }
    internal constructor(id: Int, name: String) {
        this.id = id
        this.name = name
    }
}