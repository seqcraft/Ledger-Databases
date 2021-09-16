package net.quiltservertools.ledger.databases

import com.github.quiltservertools.ledger.Ledger
import com.github.quiltservertools.ledger.api.DatabaseExtension
import com.github.quiltservertools.libs.com.uchuhimo.konf.ConfigSpec
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.util.WorldSavePath
import org.jetbrains.exposed.sql.Database
import kotlin.io.path.pathString

class LedgerDatabaseExtension : DatabaseExtension {
    override fun getConfigSpecs(): List<ConfigSpec> = listOf(DatabaseExtensionSpec)

    override fun getDatabase(server: MinecraftServer): Database {
        if (Ledger.config()[DatabaseExtensionSpec.h2]) {
            return Database.connect("jdbc:h2:${server.getSavePath(WorldSavePath.ROOT).resolve("ledger.h2").toFile()};MODE=MySQL", "org.h2.Driver")
        } else if (Ledger.config()[DatabaseExtensionSpec.mySql]) {
            return Database.connect("jdbc:mysql://${Ledger.config()[DatabaseExtensionSpec.url]}", driver = "com.mysql.cj.jdbc.Driver    ",
                user = Ledger.config()[DatabaseExtensionSpec.userName], password = Ledger.config()[DatabaseExtensionSpec.password])
        }
        return sqlite(server)
    }

    override fun getIdentifier(): Identifier {
        if (Ledger.config().contains(DatabaseExtensionSpec.h2) && Ledger.config()[DatabaseExtensionSpec.h2]) {
            return h2Identifier
        } else if (Ledger.config().contains(DatabaseExtensionSpec.mySql) && Ledger.config()[DatabaseExtensionSpec.mySql]) {
            return mySqlIdentifier

        }
        return Ledger.identifier("sqlite")
    }

    private fun sqlite(server: MinecraftServer) = Database.connect(
        url = "jdbc:sqlite:${server.getSavePath(WorldSavePath.ROOT).resolve("ledger.sqlite").pathString}",
    )

    companion object {
        val h2Identifier = Ledger.identifier("h2_extension")
        val mySqlIdentifier = Ledger.identifier("mysql_extension")
    }
}