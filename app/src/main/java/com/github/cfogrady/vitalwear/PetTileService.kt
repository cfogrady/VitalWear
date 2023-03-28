package com.github.cfogrady.vitalwear

import androidx.wear.tiles.*
import androidx.wear.tiles.ColorBuilders.argb
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PetTileService : TileService() {
    private val RESOURCES_VERSION = "1"
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTimeline(
                TimelineBuilders.Timeline.Builder().addTimelineEntry(
                    TimelineBuilders.TimelineEntry.Builder().setLayout(
                        LayoutElementBuilders.Layout.Builder().setRoot(
                            LayoutElementBuilders.Text.Builder().setText("Hello World!").setFontStyle(
                                LayoutElementBuilders.FontStyle.Builder().setColor(argb(0xFFFFFFFF.toInt())).build()
                            ).build()
                        ).build()
                    ).build()
                ).build()
            ).build())
    }

    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
        )
    }

}