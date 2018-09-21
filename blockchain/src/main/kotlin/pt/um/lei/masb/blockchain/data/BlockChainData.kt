package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.Sizeable

interface BlockChainData<T> : SelfInterval<T>, DataCategory, Sizeable