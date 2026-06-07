package com.example.animedev20.ui.theme.feature.animeinfo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeDetail
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.EmissionStatus
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.theme.AnimeDevTheme
import com.example.animedev20.ui.theme.ux.AnimeDevCopy
import com.example.animedev20.ui.theme.ux.AnimeDevErrorState
import com.example.animedev20.ui.theme.ux.AnimeDevFullScreenLoading
import com.example.animedev20.ui.theme.ux.AnimeDevInfoCard

@Composable
fun AnimeDetailScreen(
    animeId: Long,
    appContainer: AppContainer = DefaultAppContainer(),
    onBack: () -> Unit,
    onTriviaRequested: (Long) -> Unit = {}
) {
    val viewModel: AnimeDetailViewModel = viewModel(
        factory = AnimeDetailViewModel.provideFactory(
            animeId = animeId,
            animeRepository = appContainer.animeRepository,
            favoritesRepository = appContainer.favoritesRepository,
            interactionRepository = appContainer.interactionRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    val isGuest by produceState(
        initialValue = true,
        key1 = appContainer
    ) {
        value = runCatching {
            appContainer.userRepository.getUserProfile().email.isBlank()
        }.getOrDefault(true)
    }

    when (val state = uiState) {
        AnimeDetailUiState.Loading -> AnimeDevFullScreenLoading(
            message = "Cargando información del anime..."
        )

        is AnimeDetailUiState.Error -> AnimeDetailError(
            message = state.message,
            onRetry = viewModel::loadAnimeDetail,
            onBack = onBack
        )

        is AnimeDetailUiState.Success -> AnimeDetailContent(
            detail = state.detail,
            isFavorite = state.isFavorite,
            isGuest = isGuest,
            onBack = onBack,
            onTrivia = { onTriviaRequested(state.detail.anime.id) },
            onFavoriteToggle = viewModel::toggleFavorite
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimeDetailContent(
    detail: AnimeDetail,
    isFavorite: Boolean,
    isGuest: Boolean,
    onBack: () -> Unit,
    onTrivia: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    val uriHandler = LocalUriHandler.current
    val mangaUrl = detail.anime.mangaUrl ?: detail.anime.mangaPlusUrl.takeIf {
        it.isNotBlank()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = detail.anime.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = AnimeDevCopy.Actions.goBack
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (!isGuest) {
                ExtendedFloatingActionButton(
                    onClick = onFavoriteToggle,
                    icon = {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = if (isFavorite) {
                                "Quitar de favoritos"
                            } else {
                                "Agregar a favoritos"
                            }
                        )
                    },
                    text = {
                        Text(
                            text = if (isFavorite) {
                                "En favoritos"
                            } else {
                                "Agregar a favoritos"
                            }
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                bottom = if (isGuest) {
                    32.dp
                } else {
                    96.dp
                }
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimeHeroSection(anime = detail.anime)
            }

            item {
                AnimePrimaryActions(
                    trailerUrl = detail.anime.trailerUrl,
                    mangaUrl = mangaUrl,
                    onTrailer = { url -> uriHandler.openUri(url) },
                    onManga = { url -> uriHandler.openUri(url) },
                    onTrivia = onTrivia
                )
            }

            item {
                AnimeQuickInfo(anime = detail.anime)
            }

            item {
                GenreSection(genres = detail.anime.genres)
            }

            item {
                AnimeSynopsisSection(synopsis = detail.anime.synopsis)
            }

            item {
                CulturalLearningSection(culturalNotes = detail.culturalNotes)
            }

            item {
                AnimeStatsSection(anime = detail.anime)
            }
        }
    }
}

@Composable
private fun AnimeHeroSection(
    anime: Anime,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        AsyncImage(
            model = anime.coverImageUrl,
            contentDescription = "Portada de ${anime.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .clearAndSetSemantics { }
                    )

                    Text(
                        text = anime.emissionStatus.toReadableText(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = anime.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            anime.originalTitle
                ?.takeIf {
                    it.isNotBlank() && !it.equals(anime.title, ignoreCase = true)
                }
                ?.let { originalTitle ->
                    Text(
                        text = originalTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.86f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            Text(
                text = "${anime.releaseYear ?: "Año pendiente"} • ${anime.durationType.toReadableText()}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun AnimePrimaryActions(
    trailerUrl: String?,
    mangaUrl: String?,
    onTrailer: (String) -> Unit,
    onManga: (String) -> Unit,
    onTrivia: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onTrivia,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Quiz,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .clearAndSetSemantics { }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Jugar trivia")
        }

        if (!trailerUrl.isNullOrBlank()) {
            OutlinedButton(
                onClick = { onTrailer(trailerUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clearAndSetSemantics { }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Ver trailer oficial")
            }
        }

        if (!mangaUrl.isNullOrBlank()) {
            OutlinedButton(
                onClick = { onManga(mangaUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clearAndSetSemantics { }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Ver manga relacionado")
            }
        }
    }
}

@Composable
private fun AnimeQuickInfo(
    anime: Anime,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimeInfoMetric(
            label = "Año",
            value = anime.releaseYear?.toString() ?: "Pendiente",
            modifier = Modifier.weight(1f)
        )

        AnimeInfoMetric(
            label = "Episodios",
            value = anime.totalEpisodes?.toString() ?: "Pendiente",
            modifier = Modifier.weight(1f)
        )

        AnimeInfoMetric(
            label = "Duración",
            value = anime.durationType.toShortReadableText(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AnimeInfoMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreSection(
    genres: List<Genre>,
    modifier: Modifier = Modifier
) {
    if (genres.isEmpty()) {
        AnimeDevInfoCard(
            title = "Géneros pendientes",
            message = "Este anime aún no tiene géneros registrados.",
            modifier = modifier.padding(horizontal = 16.dp)
        )
        return
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Géneros",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.forEach { genre ->
                AssistChip(
                    onClick = {},
                    label = { Text(text = genre.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun AnimeSynopsisSection(
    synopsis: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Sinopsis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = synopsis.ifBlank { "Sinopsis no disponible por ahora." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CulturalLearningSection(
    culturalNotes: List<String>,
    modifier: Modifier = Modifier
) {
    if (culturalNotes.isEmpty()) {
        AnimeDevInfoCard(
            title = "Fichas culturales en construcción",
            message = "Todavía no hay suficiente información cultural disponible para este anime.",
            modifier = modifier.padding(horizontal = 16.dp)
        )
        return
    }

    val cards = culturalNotes
        .filter { it.isNotBlank() }
        .map { it.toCulturalNoteUi() }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Fichas culturales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Aprende el contexto cultural, histórico y narrativo detrás de esta obra.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        cards.forEach { card ->
            CulturalNoteCard(note = card)
        }
    }
}

@Composable
private fun CulturalNoteCard(
    note: CulturalNoteUi,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = note.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                            .clearAndSetSemantics { }
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = note.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                note.badge?.let { badge ->
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Text(
                text = note.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnimeStatsSection(
    anime: Anime,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .clearAndSetSemantics { }
                )

                Text(
                    text = "Detalles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimeStatRow(
                label = "Título original",
                value = anime.originalTitle
                    ?.takeIf { it.isNotBlank() }
                    ?: "No registrado"
            )

            AnimeStatRow(
                label = "Año de estreno",
                value = anime.releaseYear?.toString() ?: "Pendiente"
            )

            AnimeStatRow(
                label = "Episodios",
                value = anime.totalEpisodes?.toString() ?: "Pendiente"
            )

            AnimeStatRow(
                label = "Duración",
                value = anime.durationType.toReadableText()
            )

            AnimeStatRow(
                label = "Estado",
                value = anime.emissionStatus.toReadableText()
            )
        }
    }
}

@Composable
private fun AnimeStatRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AnimeDetailError(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimeDevErrorState(
            title = "No pudimos cargar este anime",
            message = message,
            onPrimaryAction = onRetry,
            secondaryActionLabel = "Volver",
            onSecondaryAction = onBack
        )
    }
}

private data class CulturalNoteUi(
    val category: String,
    val title: String,
    val body: String,
    val badge: String? = null,
    val icon: ImageVector
)

private fun String.toCulturalNoteUi(): CulturalNoteUi {
    val cleanNote = trim()
        .removePrefix("•")
        .trim()

    val normalized = cleanNote
        .lowercase()
        .normalizeForCulturalParsing()

    return when {
        normalized.startsWith("glosario cultural") -> {
            val rawTerm = cleanNote
                .substringAfter("—", missingDelimiterValue = "")
                .ifBlank {
                    cleanNote.substringAfter("-", missingDelimiterValue = "")
                }
                .trim()

            val term = rawTerm
                .substringBefore(":", missingDelimiterValue = "Término cultural")
                .trim()
                .ifBlank { "Término cultural" }

            val body = rawTerm
                .substringAfter(":", missingDelimiterValue = cleanNote)
                .trim()
                .ifBlank { cleanNote }

            CulturalNoteUi(
                category = "Glosario cultural",
                title = term,
                body = body,
                badge = "Concepto",
                icon = Icons.Filled.MenuBook
            )
        }

        normalized.startsWith("ambientacion") -> CulturalNoteUi(
            category = "Contexto de época",
            title = cleanNote.titleBeforeColon(defaultTitle = "Ambientación"),
            body = cleanNote.bodyAfterColon(),
            badge = "Contexto",
            icon = Icons.Filled.Info
        )

        normalized.startsWith("temporada original") -> CulturalNoteUi(
            category = "Calendario japonés",
            title = "Temporada original",
            body = cleanNote.bodyAfterColon(),
            badge = "Emisión",
            icon = Icons.Filled.Star
        )

        normalized.startsWith("periodo de emision") -> CulturalNoteUi(
            category = "Calendario japonés",
            title = "Periodo de emisión",
            body = cleanNote.bodyAfterColon(),
            badge = "Emisión",
            icon = Icons.Filled.Star
        )

        normalized.startsWith("emision japonesa") -> CulturalNoteUi(
            category = "Televisión japonesa",
            title = "Emisión japonesa",
            body = cleanNote.bodyAfterColon(),
            badge = "TV",
            icon = Icons.Filled.Star
        )

        normalized.startsWith("origen de la obra") -> CulturalNoteUi(
            category = "Origen narrativo",
            title = "Origen de la obra",
            body = cleanNote.bodyAfterColon(),
            badge = "Fuente",
            icon = Icons.Filled.MenuBook
        )

        normalized.startsWith("produccion y estilo visual") -> CulturalNoteUi(
            category = "Producción",
            title = "Estudio y estilo visual",
            body = cleanNote.bodyAfterColon(),
            badge = "Estudio",
            icon = Icons.Filled.Info
        )

        normalized.startsWith("industria del anime") -> CulturalNoteUi(
            category = "Industria cultural",
            title = "Comité de producción",
            body = cleanNote.bodyAfterColon(),
            badge = "Industria",
            icon = Icons.Filled.Info
        )

        normalized.startsWith("lectura cultural de los generos") -> CulturalNoteUi(
            category = "Lectura cultural",
            title = "Géneros y expectativas narrativas",
            body = cleanNote.bodyAfterColon(),
            badge = "Análisis",
            icon = Icons.Filled.MenuBook
        )

        normalized.startsWith("temas narrativos relevantes") -> CulturalNoteUi(
            category = "Temas narrativos",
            title = "Conflictos y referencias",
            body = cleanNote.bodyAfterColon(),
            badge = "Temas",
            icon = Icons.Filled.MenuBook
        )

        normalized.startsWith("demografia editorial") -> CulturalNoteUi(
            category = "Demografía editorial",
            title = "Tradición de publicación",
            body = cleanNote.bodyAfterColon(),
            badge = "Editorial",
            icon = Icons.Filled.MenuBook
        )

        normalized.startsWith("dato de contexto") -> CulturalNoteUi(
            category = "Dato de contexto",
            title = "Información complementaria",
            body = cleanNote.bodyAfterColon(),
            badge = "Extra",
            icon = Icons.Filled.Info
        )

        normalized.startsWith("contexto de estreno") -> CulturalNoteUi(
            category = "Contexto histórico",
            title = "Contexto de estreno",
            body = cleanNote.bodyAfterColon(),
            badge = "Época",
            icon = Icons.Filled.Star
        )

        else -> CulturalNoteUi(
            category = "Ficha cultural",
            title = cleanNote.titleBeforeColon(defaultTitle = "Aprendizaje cultural"),
            body = cleanNote.bodyAfterColon(),
            badge = "Nota",
            icon = Icons.Filled.Info
        )
    }
}

private fun String.titleBeforeColon(defaultTitle: String): String {
    return substringBefore(":", missingDelimiterValue = defaultTitle)
        .trim()
        .ifBlank { defaultTitle }
}

private fun String.bodyAfterColon(): String {
    return substringAfter(":", missingDelimiterValue = this)
        .trim()
        .ifBlank { this }
}

private fun String.normalizeForCulturalParsing(): String {
    return this
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace("ü", "u")
        .replace("ñ", "n")
}

private fun DurationType.toReadableText(): String {
    return when (this) {
        DurationType.SHORT -> "Corto (≤15 min)"
        DurationType.MEDIUM -> "Medio (16-25 min)"
        DurationType.LONG -> "Largo (30+ min)"
    }
}

private fun DurationType.toShortReadableText(): String {
    return when (this) {
        DurationType.SHORT -> "Corta"
        DurationType.MEDIUM -> "Media"
        DurationType.LONG -> "Larga"
    }
}

private fun EmissionStatus.toReadableText(): String {
    return when (this) {
        EmissionStatus.ON_AIR -> "En emisión"
        EmissionStatus.FINISHED -> "Finalizado"
        EmissionStatus.ON_BREAK -> "En pausa"
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimeDetailPreview() {
    AnimeDevTheme {
        Surface {
            AnimeDetailContent(
                detail = FakeDataSource.getAnimeDetail(FakeDataSource.heroAnime.id),
                isFavorite = true,
                isGuest = false,
                onBack = {},
                onTrivia = {},
                onFavoriteToggle = {}
            )
        }
    }
}