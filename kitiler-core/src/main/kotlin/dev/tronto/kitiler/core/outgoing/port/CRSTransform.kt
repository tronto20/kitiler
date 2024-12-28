package dev.tronto.kitiler.core.outgoing.port

interface CRSTransform : CoordinateTransform {
    object Empty : CRSTransform, CoordinateTransform by CoordinateTransform.Empty
}
