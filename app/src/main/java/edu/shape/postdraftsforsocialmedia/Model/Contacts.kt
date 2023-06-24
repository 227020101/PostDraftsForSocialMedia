package edu.shape.postdraftsforsocialmedia.Model

class Contacts {
    var id = 0
    var name: String
    internal constructor(name: String) {
        this.name = name
    }
    constructor(id: Int, name: String) {
        this.id = id
        this.name = name
    }
}