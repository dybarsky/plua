package dmax.plua.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dmax.plua.DataSource;
import dmax.plua.domain.Language;
import dmax.plua.persist.DataBaseManager;
import dmax.plua.settings.Settings;

/**
 * Created by Maksym Dybarskyi | maksym.dybarskyi@symphonyteleca.com
 * on 14.09.15 at 14:34
 */
@Module
public class DependenciesProvider {

    private static final Language DEFAULT = Language.POLISH;
    private Context context;

    public DependenciesProvider(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    public DataSource provideDataSource(DataBaseManager manager, Settings settings) {
        return new DataSource(manager, DEFAULT, settings);
    }

    @Provides @Singleton
    public Settings provideSettings() {
        return new Settings(context);
    }

    @Provides @Singleton
    public DataBaseManager provideDatabaseManager() {
        return new DataBaseManager(context);
    }
}
