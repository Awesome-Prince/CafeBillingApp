package com.cafe.billing.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import com.cafe.billing.data.models.SalesOrder
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// BILL PRINT ADAPTER
// Custom PrintDocumentAdapter that renders the bill receipt
// as a PDF page, compatible with the Android print framework.
// Works with any AirPrint / IPP printer and "Save as PDF".
//
// For thermal printers: connect via Bluetooth printing apps or
// use the PDF output and send via ESC/POS apps.
// ============================================================

class BillPrintAdapter(
    private val context: Context,
    private val order: SalesOrder
) : PrintDocumentAdapter() {

    private var pdfDocument: PrintedPdfDocument? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        pdfDocument = PrintedPdfDocument(context, newAttributes)

        val info = PrintDocumentInfo.Builder("bill_${order.id}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()

        callback.onLayoutFinished(info, newAttributes != oldAttributes)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        val doc = pdfDocument ?: return

        val page = doc.startPage(0)
        drawBill(page.canvas)
        doc.finishPage(page)

        try {
            doc.writeTo(FileOutputStream(destination.fileDescriptor))
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        } finally {
            doc.close()
        }
    }

    // ── Drawing ───────────────────────────────────────────

    /**
     * Draws the full bill receipt onto a PDF canvas.
     * All dimensions are in points (72 pts = 1 inch).
     */
    private fun drawBill(canvas: Canvas) {
        val cartItems = order.getCartItems()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(order.timestamp))

        // ── Paint styles ──
        val titlePaint = Paint().apply {
            textSize = 22f
            color = Color.BLACK
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
            textAlign = Paint.Align.CENTER
        }
        val bodyPaint = Paint().apply {
            textSize = 13f
            color = Color.BLACK
        }
        val smallPaint = Paint().apply {
            textSize = 11f
            color = Color.GRAY
        }
        val boldPaint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
            isFakeBoldText = true
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val totalPaint = Paint().apply {
            textSize = 16f
            color = Color.BLACK
            isFakeBoldText = true
        }

        val pageWidth = canvas.width.toFloat()
        val margin = 40f
        var y = 60f

        // ── Header ──
        canvas.drawText("☕ CAFÉ RECEIPT", pageWidth / 2, y, titlePaint); y += 28f
        canvas.drawText("Order #${order.id}", pageWidth / 2, y, headerPaint); y += 18f
        canvas.drawText(dateStr, pageWidth / 2, y, smallPaint); y += 30f

        // Divider
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 18f

        // ── Column headers ──
        canvas.drawText("Item", margin, y, boldPaint)
        canvas.drawText("Qty", pageWidth * 0.55f, y, boldPaint)
        canvas.drawText("Price", pageWidth * 0.70f, y, boldPaint)
        canvas.drawText("Total", pageWidth * 0.85f, y, boldPaint)
        y += 6f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 16f

        // ── Line items ──
        for (item in cartItems) {
            // Truncate long names
            val name = if (item.menuItem.name.length > 20)
                item.menuItem.name.take(20) + "…"
            else item.menuItem.name

            canvas.drawText(name, margin, y, bodyPaint)
            canvas.drawText("x${item.quantity}", pageWidth * 0.55f, y, bodyPaint)
            canvas.drawText("₹${"%.2f".format(item.menuItem.price)}", pageWidth * 0.68f, y, bodyPaint)
            canvas.drawText("₹${"%.2f".format(item.lineTotal)}", pageWidth * 0.84f, y, bodyPaint)
            y += 20f
        }

        // Divider before total
        y += 4f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint); y += 18f

        // ── Total ──
        canvas.drawText("TOTAL AMOUNT:", margin, y, totalPaint)
        canvas.drawText(
            "₹${"%.2f".format(order.totalAmount)}",
            pageWidth - margin,
            y,
            totalPaint.apply { textAlign = Paint.Align.RIGHT }
        )
        y += 40f

        // ── Footer ──
        val footerPaint = Paint().apply {
            textSize = 12f
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Thank you for visiting! 🙏", pageWidth / 2, y, footerPaint)
    }
}
