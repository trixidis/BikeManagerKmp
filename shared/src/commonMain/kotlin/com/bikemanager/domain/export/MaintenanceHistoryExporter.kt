package com.bikemanager.domain.export

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance

/**
 * Génère l'historique des entretiens d'une moto en PDF et propose son partage natif.
 * Implémentation platform-specific (Android : android.graphics.pdf + Intent.ACTION_SEND,
 * iOS : UIGraphicsPDFRenderer + UIActivityViewController).
 */
interface MaintenanceHistoryExporter {
    /**
     * @param bikeName Nom de la moto, affiché en en-tête du document
     * @param countingMethod Unité (km ou heures) utilisée pour afficher la valeur de chaque entretien
     * @param maintenances Entretiens à inclure dans le document, dans l'ordre d'affichage souhaité
     */
    suspend fun exportAndShare(
        bikeName: String,
        countingMethod: CountingMethod,
        maintenances: List<Maintenance>
    ): Result<Unit>
}
