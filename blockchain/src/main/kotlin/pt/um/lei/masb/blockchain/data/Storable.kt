package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement

interface Storable {
    fun store(): OElement
}