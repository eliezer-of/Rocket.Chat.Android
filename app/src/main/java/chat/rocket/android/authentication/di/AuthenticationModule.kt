package chat.rocket.android.authentication.di

import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class AuthenticationModule {

    @Provides
    @PerActivity
    fun provideAuthenticationNavigator(activity: AuthenticationActivity) = AuthenticationNavigator(activity)

    @Provides
    @PerActivity
    fun provideAuthTokenRepository(): AuthTokenRepository {
        return AuthTokenRepository()
    }

    @Provides
    fun provideJob(): Job {
        return Job()
    }
}