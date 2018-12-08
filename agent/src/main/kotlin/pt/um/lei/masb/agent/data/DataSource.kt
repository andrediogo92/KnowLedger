package pt.um.lei.masb.agent.data

import java.net.URI


open class DataSource(val id: String, val uri: URI) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataSource) return false
        if (!super.equals(other)) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "DataSource(id='$id')"
    }


}
