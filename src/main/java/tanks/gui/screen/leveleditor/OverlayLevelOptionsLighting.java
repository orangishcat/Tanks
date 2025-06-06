package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;
import tanks.gui.screen.Screen;

public class OverlayLevelOptionsLighting extends ScreenLevelEditorOverlay
{
    public TextBoxSlider light;
    public TextBoxSlider shadow;

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Back", this::escape
    );

    public OverlayLevelOptionsLighting(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        light = new TextBoxSlider(this.centerX, this.centerY - this.objYSpace * 0.75, this.objWidth, this.objHeight, "Direct light", () ->
        {
            if (light.inputText.isEmpty())
                light.inputText = light.previousInputText;

            screenLevelEditor.level.light = Integer.parseInt(light.inputText) / 100.0;
            Level.currentLightIntensity = screenLevelEditor.level.light;
        }
                , (int) Math.round(screenLevelEditor.level.light * 100), 0, 200, 1);

        light.allowLetters = false;
        light.allowSpaces = false;
        light.maxChars = 3;
        light.checkMaxValue = true;
        light.integer = true;

        light.r1 = 0;
        light.g1 = 0;
        light.b1 = 0;

        shadow = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace * 0.75, this.objWidth, this.objHeight, "Shadow light", () ->
        {
            if (shadow.inputText.isEmpty())
                shadow.inputText = shadow.previousInputText;

            screenLevelEditor.level.shadow = Integer.parseInt(shadow.inputText) / 100.0;
            Level.currentShadowIntensity = screenLevelEditor.level.shadow;
        }
                , (int) Math.round(screenLevelEditor.level.shadow * 100), 0, 200, 1);

        shadow.allowLetters = false;
        shadow.allowSpaces = false;
        shadow.maxChars = 3;
        shadow.checkMaxValue = true;
        shadow.integer = true;

        shadow.r1 = 0;
        shadow.g1 = 0;
        shadow.b1 = 0;
    }

    public void update()
    {
        this.light.update();
        this.shadow.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        this.light.draw();
        this.shadow.draw();
        this.back.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Lighting");
    }
}
