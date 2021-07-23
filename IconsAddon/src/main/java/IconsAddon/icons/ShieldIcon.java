package IconsAddon.icons;

import IconsAddon.util.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class ShieldIcon extends AbstractDamageTypeIcon {

    private static ShieldIcon singleton;

    public static ShieldIcon get()
    {
        if (singleton == null) {
            singleton = new ShieldIcon();
        }
        return singleton;
    }


    @Override
    public String name() {
        return "Shield";
    }

    @Override
    public TextureAtlas.AtlasRegion getTexture() {
        Texture tex = TextureLoader.getTexture("IconsAddonResources/images/icons/Shield.png");
        return new TextureAtlas.AtlasRegion(tex, 0, 0, IMG_SIZE, IMG_SIZE);
    }
}
