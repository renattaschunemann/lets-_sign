package br.com.letssign.core.common.contacts

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts

data class ContatoMockAgenda(
    val id: String,
    val nome: String,
    val email: String,
    val telefone: String,
    val cargo: String
)

object AgendaContatosMockHelper {

    val CONTATOS_MOCKADOS = listOf(
        ContatoMockAgenda(
            id = "c1",
            nome = "Carlos Alberto Silva",
            email = "carlos.silva@parceiro.com.br",
            telefone = "11987654321",
            cargo = "Diretor Comercial"
        ),
        ContatoMockAgenda(
            id = "c2",
            nome = "Mariana Souza Santos",
            email = "mariana.souza@consultoria.com.br",
            telefone = "11998765432",
            cargo = "Gerente Jurídica"
        ),
        ContatoMockAgenda(
            id = "c3",
            nome = "Roberto Dias Mendonça",
            email = "roberto.dias@techlog.com.br",
            telefone = "21976543210",
            cargo = "Sócio Fundador"
        ),
        ContatoMockAgenda(
            id = "c4",
            nome = "Fernanda Oliveira Lima",
            email = "fernanda.lima@advocacia.com.br",
            telefone = "31985214789",
            cargo = "Advogada Titular"
        ),
        ContatoMockAgenda(
            id = "c5",
            nome = "Juliana Castro Ribeiro",
            email = "juliana.castro@imoveis.com.br",
            telefone = "41991234567",
            cargo = "Locadora / Proprietária"
        ),
        ContatoMockAgenda(
            id = "c6",
            nome = "Marcelo Henrique Costa",
            email = "marcelo.costa@logistica.com.br",
            telefone = "19981239876",
            cargo = "Comprador Executivo"
        ),
        ContatoMockAgenda(
            id = "c7",
            nome = "Patricia Antunes Barbosa",
            email = "patricia.barbosa@financas.com.br",
            telefone = "51993456789",
            cargo = "Supervisora Financeira"
        ),
        ContatoMockAgenda(
            id = "c8",
            nome = "Bruno Cesar Fagundes",
            email = "bruno.fagundes@varejo.com.br",
            telefone = "71988776655",
            cargo = "Representante Comercial"
        ),
        ContatoMockAgenda(
            id = "c9",
            nome = "Larissa Martins Gomes",
            email = "larissa.gomes@saude.com.br",
            telefone = "61982345678",
            cargo = "Diretora de Operações"
        ),
        ContatoMockAgenda(
            id = "c10",
            nome = "Thiago Moreira Pinto",
            email = "thiago.moreira@engenharia.com.br",
            telefone = "81994567890",
            cargo = "Engenheiro Responsável"
        )
    )

    fun inserirContatosNoDispositivoSeNecessario(context: Context) {
        try {
            val cr = context.contentResolver
            val cursor = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts._ID),
                null,
                null,
                null
            )
            val contatosExistentes = cursor?.count ?: 0
            cursor?.close()

            if (contatosExistentes >= 10) return

            for (contato in CONTATOS_MOCKADOS) {
                val ops = ArrayList<ContentProviderOperation>()

                val rawContactInsertIndex = ops.size
                ops.add(
                    ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                        .withValue(RawContacts.ACCOUNT_TYPE, null)
                        .withValue(RawContacts.ACCOUNT_NAME, null)
                        .build()
                )

                // Nome
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(StructuredName.DISPLAY_NAME, contato.nome)
                        .build()
                )

                // Telefone
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        .withValue(Phone.NUMBER, contato.telefone)
                        .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                        .build()
                )

                // Email
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                        .withValue(Email.DATA, contato.email)
                        .withValue(Email.TYPE, Email.TYPE_WORK)
                        .build()
                )

                cr.applyBatch(ContactsContract.AUTHORITY, ops)
            }
        } catch (_: Exception) {
            // Silencioso se não tiver permissões no manifest do sistema operacional
        }
    }
}
