package br.com.letssign.presentation.signatario.assinatura

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.letssign.data.local.dao.AssinaturaDao
import br.com.letssign.data.local.dao.CampoAssinaturaDao
import br.com.letssign.data.local.dao.DocumentoDao
import br.com.letssign.data.local.dao.ParticipanteDocumentoDao
import br.com.letssign.data.local.dao.VersaoDocumentoDao
import br.com.letssign.data.local.entities.AssinaturaEntity
import br.com.letssign.data.local.entities.CampoAssinaturaEntity
import br.com.letssign.data.local.entities.DocumentoEntity
import br.com.letssign.data.local.entities.ParticipanteDocumentoEntity
import br.com.letssign.data.local.entities.VersaoDocumentoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Sig08VisualizadorUiState(
    val carregando: Boolean = true,
    val documento: DocumentoEntity? = null,
    val versao: VersaoDocumentoEntity? = null,
    val participantes: List<ParticipanteDocumentoEntity> = emptyList(),
    val campos: List<CampoAssinaturaEntity> = emptyList(),
    val assinaturasPorCampo: Map<String, AssinaturaEntity> = emptyMap(),
    val paginaAtual: Int = 1,
    val totalPaginas: Int = 1
)

@HiltViewModel
class Sig08VisualizadorLeituraViewModel @Inject constructor(
    private val documentoDao: DocumentoDao,
    private val versaoDao: VersaoDocumentoDao,
    private val participanteDao: ParticipanteDocumentoDao,
    private val campoDao: CampoAssinaturaDao,
    private val assinaturaDao: AssinaturaDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val documentoId: String = savedStateHandle.get<String>("documentoId") ?: ""

    private val _uiState = MutableStateFlow(Sig08VisualizadorUiState())
    val uiState: StateFlow<Sig08VisualizadorUiState> = _uiState.asStateFlow()

    init {
        carregarDocumento()
    }

    private fun carregarDocumento() {
        viewModelScope.launch {
            if (documentoId.isBlank()) {
                _uiState.update { it.copy(carregando = false) }
                return@launch
            }

            val doc = documentoDao.buscarPorId(documentoId)
            val versao = versaoDao.buscarVersaoAtual(documentoId)
            val versaoId = versao?.id ?: ""

            val parts = if (versaoId.isNotBlank()) participanteDao.listarPorVersaoSync(versaoId) else emptyList()
            val campos = if (versaoId.isNotBlank()) campoDao.listarPorVersaoSync(versaoId) else emptyList()
            val assinaturas = if (campos.isNotEmpty()) {
                assinaturaDao.listarPorCamposSync(campos.map { it.id })
            } else emptyList()
            val assinaturasMap = assinaturas.associateBy { it.campoAssinaturaId }

            val maxPagina = maxOf(1, campos.maxOfOrNull { it.numeroPagina } ?: 1)

            _uiState.update {
                it.copy(
                    carregando = false,
                    documento = doc,
                    versao = versao,
                    participantes = parts,
                    campos = campos,
                    assinaturasPorCampo = assinaturasMap,
                    totalPaginas = maxPagina
                )
            }
        }
    }

    fun mudarPagina(novaPagina: Int) {
        val total = _uiState.value.totalPaginas.coerceAtLeast(1)
        val clamped = novaPagina.coerceIn(1, total)
        _uiState.update { it.copy(paginaAtual = clamped) }
    }
}

