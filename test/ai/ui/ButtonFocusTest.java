package ai.ui;

/** Checks the Swing focus regression that caused Space to activate Reset Match. */
public final class ButtonFocusTest {
    private ButtonFocusTest() {
    }

    public static void main(String[] args) {
        DarkButton reset = new DarkButton("Reset Match", DarkButton.Style.NORMAL);
        if (reset.isFocusable() || reset.isRequestFocusEnabled()) {
            throw new AssertionError("Game buttons must not steal Space from the Hold shortcut.");
        }
        System.out.println("Button focus test passed.");
    }
}
