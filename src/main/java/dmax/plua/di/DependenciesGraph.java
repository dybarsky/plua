package dmax.plua.di;

import javax.inject.Singleton;

import dagger.Component;
import dmax.plua.ui.MainActivity;
import dmax.plua.ui.cards.CardsFragment;
import dmax.plua.ui.detail.LinkDetailFragment;

/**
 * Created by Maksym Dybarskyi | maksym.dybarskyi@symphonyteleca.com
 * on 14.09.15 at 14:29
 */
@Singleton
@Component(modules = DependenciesProvider.class)
public interface DependenciesGraph {
    void inject(MainActivity activity);
    void inject(LinkDetailFragment fragment);
    void inject(CardsFragment fragment);
}
