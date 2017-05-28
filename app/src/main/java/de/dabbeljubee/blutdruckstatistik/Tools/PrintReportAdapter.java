package de.dabbeljubee.blutdruckstatistik.Tools;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.graphics.pdf.PdfDocument.Page;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import de.dabbeljubee.blutdruckstatistik.Logic.DataProvider;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementStatisticItem;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementStatisticMonth;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementStatisticWeek;
import de.dabbeljubee.blutdruckstatistik.MainActivity;
import de.dabbeljubee.blutdruckstatistik.R;
import org.joda.time.LocalDate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class PrintReportAdapter extends PrintDocumentAdapter {

    private static final Logger LOGGER = Logger.getLogger("PrintReportAdapter");

    private final MainActivity mainActivity;
    private final DataProvider dataProvider = DataProvider.getDataProvider();
    private PrintedPdfDocument mPdfDocument;
    private int pageHeight;
    private int pageWidth;
    private List<Integer> startMonthOfPage = new ArrayList<>(Arrays.asList(0));
    private int totalPages = startMonthOfPage.size();

    public int maxLinesPerPage = 30;

    public PrintReportAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
        mPdfDocument = new PrintedPdfDocument(mainActivity, newAttributes);
        pageHeight = newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
        pageWidth = newAttributes.getMediaSize().getWidthMils() / 1000 * 72;
        totalPages = computePageCount(newAttributes);

        if (cancellationSignal.isCanceled() ) {
            callback.onLayoutCancelled();
            return;
        }

        if (0 < totalPages) {
            PrintDocumentInfo info = new PrintDocumentInfo.Builder(mainActivity.getResources().getString(R.string.report_name, LocalDate.now().toString("yyyyMMdd")))
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalPages)
                    .build();
            callback.onLayoutFinished(info, true);
        } else {
            callback.onLayoutFailed("Page count is zero.");
        }
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        for (int i = 0; i < totalPages; i++) {
            if (pageInRange(pages, i))
            {
                PageInfo newPage = new PageInfo.Builder(pageWidth, pageHeight, i).create();

                Page page =  mPdfDocument.startPage(newPage);

                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    mPdfDocument.close();
                    mPdfDocument = null;
                    return;
                }
                drawPage(page, i);
                mPdfDocument.finishPage(page);
            }
        }

        try {
            mPdfDocument.writeTo(new FileOutputStream(
                    destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            mPdfDocument.close();
            mPdfDocument = null;
        }

        callback.onWriteFinished(pages);
    }

    private int computePageCount(PrintAttributes printAttributes) {
        maxLinesPerPage = 40;

        PrintAttributes.MediaSize pageSize = printAttributes.getMediaSize();
        if (!pageSize.isPortrait()) {
            maxLinesPerPage = 30;
        }

        int currentMonth = 0;
        int linesPerPage = 0;

        while(currentMonth < dataProvider.getMonthsMap().size()) {
            linesPerPage++;
            linesPerPage += dataProvider.getMonthsAscending().get(0).getRelatedWeeks().size();

            if (linesPerPage > maxLinesPerPage) {
                startMonthOfPage.add(currentMonth - 1);

                linesPerPage = 0;

                LOGGER.fine(String.format("Page %d starts with month %d", startMonthOfPage.size() +1, currentMonth));
            } else {
                currentMonth++;
            }
        }

        return startMonthOfPage.size();
    }

    private boolean pageInRange(PageRange[] pages, int page)
    {
        for (int i = 0; i < pages.length; i++)
        {
            if (page >= pages[i].getStart() && page <= pages[i].getEnd())
                return true;
        }
        return false;
    }

    private void drawPage(PdfDocument.Page page, int pageNo) {
        Canvas canvas = page.getCanvas();
        canvas.translate(30, 0);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setStrokeWidth(paint.getStrokeWidth() * 2);

        printTitle(canvas, paint, pageNo);
        canvas.translate(0, 35);
        canvas.save();
        printHeader(canvas, paint);

        int lines = 0;

        int endMonth = pageNo < totalPages - 1 ? startMonthOfPage.get(pageNo + 1) : dataProvider.getMonthsMap().size();

        for(int monthNo = startMonthOfPage.get(pageNo); monthNo < endMonth; monthNo++) {
            final MeasurementStatisticMonth month = dataProvider.getMonthsAscending().get(monthNo);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            printLine(canvas, month, paint);
            lines++;
            paint.setTypeface(Typeface.DEFAULT);
            List<MeasurementStatisticWeek> weeksPerMonth = dataProvider.getWeeksOfMonthAscending(month.getFirstDayAsLong());
            for (int weekNo = 0; weekNo < weeksPerMonth.size(); weekNo++) {
                printLine(canvas, weeksPerMonth.get(weekNo), paint);
                lines++;
            }
        }

        canvas.restore();

        int fieldWidth = (canvas.getWidth() - 60) / 14;
        final int stopY = (lines + 3) * 15 + 5;
        canvas.drawLine(fieldWidth * 2 - 10, 0, fieldWidth * 2 - 10, stopY, paint);
        canvas.drawLine(fieldWidth * 6 - 5, 0, fieldWidth * 6 - 5, stopY, paint);
        canvas.drawLine(fieldWidth * 10 - 5, 0, fieldWidth * 10 - 5, stopY, paint);
    }

    private void printTitle(Canvas canvas, Paint paint, int pageNo) {
        paint.setTextSize(20);
        canvas.translate(0, 30);
        canvas.drawText(mainActivity.getResources().getString(R.string.report_title_line, LocalDate.now().toString("dd.MM.yyyy"), pageNo + 1), 0, 0, paint);
    }

    private void printHeader(Canvas canvas, Paint paint) {
        int fieldWidth = (canvas.getWidth() - 60) / 14;
        canvas.translate(0, 15);
        paint.setTextSize(10);

        canvas.translate(fieldWidth * 2, 0);
        drawField(canvas, "SYS", fieldWidth, paint);
        drawField(canvas, "SYS", fieldWidth, paint);
        drawField(canvas, "SYS", fieldWidth, paint);
        drawField(canvas, "SYS", fieldWidth, paint);
        drawField(canvas, "DIA", fieldWidth, paint);
        drawField(canvas, "DIA", fieldWidth, paint);
        drawField(canvas, "DIA", fieldWidth, paint);
        drawField(canvas, "DIA", fieldWidth, paint);
        drawField(canvas, "Pulse", fieldWidth, paint);
        drawField(canvas, "Pulse", fieldWidth, paint);
        drawField(canvas, "Pulse", fieldWidth, paint);
        drawField(canvas, "Pulse", fieldWidth, paint);

        canvas.translate(fieldWidth * -14, 15);

        canvas.drawText(mainActivity.getResources().getString(R.string.statistics_header_time), 0, 0, paint);
        canvas.translate(fieldWidth * 2, 0);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.sysLevels.getAlarm()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.sysLevels.getWarning()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.sysLevels.getLow()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_greater, DataProvider.sysLevels.getLow()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.diaLevels.getAlarm()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.diaLevels.getWarning()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.diaLevels.getLow()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_greater, DataProvider.diaLevels.getLow()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.pulseLevels.getAlarm()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.pulseLevels.getWarning()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_less, DataProvider.pulseLevels.getLow()), fieldWidth, paint);
        drawField(canvas, mainActivity.getResources().getString(R.string.statistics_header_level_greater, DataProvider.pulseLevels.getLow()), fieldWidth, paint);

        canvas.translate(fieldWidth * -14, 10);
        canvas.drawLine(0, 0, fieldWidth * 14, 0, paint);
    }

    private void printLine(Canvas canvas, MeasurementStatisticItem item, Paint paint) {
        LOGGER.fine(String.format("Print %s", item.getFirstDayAsString(mainActivity.getResources())));

        int fieldWidth = (canvas.getWidth() - 60) / 14;
        canvas.translate(0, 15);
        paint.setTextSize(10);

        canvas.drawText(item.getFirstDayAsString(mainActivity.getResources()), 0, 0, paint);
        canvas.translate(fieldWidth * 2, 0);
        drawField(canvas, formattedPercentage(item.getSystolic().getAlarmRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getSystolic().getWarningRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getSystolic().getNormalRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getSystolic().getLowRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getDiastolic().getAlarmRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getDiastolic().getWarningRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getDiastolic().getNormalRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getDiastolic().getLowRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getPulse().getAlarmRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getPulse().getWarningRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getPulse().getNormalRatio()), fieldWidth, paint);
        drawField(canvas, formattedPercentage(item.getPulse().getLowRatio()), fieldWidth, paint);

        canvas.translate(fieldWidth * -14, 0);
    }

    private void drawField(Canvas canvas, String text, int fieldWidth, Paint paint) {
        canvas.drawText(text, 0, 0, paint);
        canvas.translate(fieldWidth, 0);
    }

    private String formattedPercentage(BigDecimal percentage) {
        return String.format("%4.1f", percentage.doubleValue());
    }
}
