package dev.vectorpatch.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.vectorpatch.patcher.LSPatcher
import dev.vectorpatch.service.ShizukuManager
import dev.vectorpatch.updater.UpdateChecker
import dev.vectorpatch.updater.GitHubUpdateChecker
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideShizukuManager(): ShizukuManager = ShizukuManager()

    @Provides @Singleton
    fun provideLSPatcher(@ApplicationContext ctx: Context): LSPatcher = LSPatcher(ctx)

    @Provides @Singleton
    fun provideUpdateChecker(
        @ApplicationContext ctx: Context
    ): UpdateChecker = GitHubUpdateChecker(ctx, "realheckerrr-bit", "VectorPatch")
}
