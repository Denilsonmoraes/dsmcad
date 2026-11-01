package com.paginacaopro.app.model

enum class BondType(val displayName: String, val description: String) {
    RUNNING_HALF(
        "Amarração alternada (1/2)",
        "Amarração tradicional com trespasse de meio bloco entre fiadas consecutivas."
    ),
    RUNNING_THIRD(
        "Amarração em terço (1/3)",
        "Trespasse de um terço do bloco, comum em formatos longos ou paginações especiais."
    ),
    STACK_BOND(
        "Junta a prumo (Alinhada)",
        "Juntas verticais contínuas e alinhadas (exige armadura de amarração conforme normas)."
    )
}
