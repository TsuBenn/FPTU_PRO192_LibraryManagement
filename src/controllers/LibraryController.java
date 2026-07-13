package controllers;

import config.AppContext;
import config.DataSeeder;
import controllers.handlers.*;
import utilities.InputController;
import utilities.UIRender;

public class LibraryController {
    private final BookHandler bookHandler;
    private final MemberHandler memberHandler;
    private final CirculationHandler circulationHandler;
    private final ReportHandler reportHandler;
    private final InputController input;

    public LibraryController() {
        AppContext ctx = new AppContext();

        this.input = ctx.input;
        this.bookHandler = new BookHandler(ctx);
        this.memberHandler = new MemberHandler(ctx);
        this.circulationHandler = new CirculationHandler(ctx);
        this.reportHandler = new ReportHandler(ctx);
    }

    public void start() {
        boolean running = true;
        String[] modules = {
                "Book Management", "Member Management",
                "Borrow Book", "Return Book", "Report Lost Book",
                "Analytics Dashboard",
                "Resolve fine money"
        };
        while (running) {
            UIRender.clearScreen();
            UIRender.renderMenu("Library System", modules);
            try {
                switch (input.getChoice("Select: ", 7)) {
                    case 1:
                        bookHandler.handle();
                        break;
                    case 2:
                        memberHandler.handle();
                        break;
                    case 3:
                        circulationHandler.handleBorrow();
                        break;
                    case 4:
                        circulationHandler.handleReturn();
                        break;
                    case 5:
                        circulationHandler.handleLost();
                        break;
                    case 6:
                        reportHandler.handle();
                        break;
                    case 7:
                        circulationHandler.handlePayment();
                        break;
                    case 0:
                        running = false;
                        break;
                }
            } catch (RuntimeException e) {
                UIRender.renderError(e.getMessage());
        }
    }
}
}
