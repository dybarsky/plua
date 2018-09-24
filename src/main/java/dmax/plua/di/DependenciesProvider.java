package dmax.plua.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dmax.plua.DataSource;
import dmax.plua.domain.Language;
import dmax.plua.persist.DataBaseManager;

/**
 * Created by Maksym Dybarskyi | maksym.dybarskyi@symphonyteleca.com
 * on 14.09.15 at 14:34
 */
@Module
public class DependenciesProvider {

    private static final Language DEFAULT = Language.GERMAN;
    private Context context;

    public DependenciesProvider(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    public DataSource provideDataSource(DataBaseManager manager) {
        return new DataSource(manager, DEFAULT);
    }

    @Provides @Singleton
    public DataBaseManager provideDatabaseManager() {
        return new DataBaseManager(context);
    }
}
