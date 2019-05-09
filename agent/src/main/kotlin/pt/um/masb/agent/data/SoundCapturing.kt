package pt.um.masb.agent.data

import mu.KotlinLogging
import java.math.BigDecimal
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import kotlin.experimental.and

private val logger = KotlinLogging.logger {}

fun captureSound(): Pair<BigDecimal, BigDecimal>? {
    var max: Short = -1
    var bytesRead: Int
    val line: TargetDataLine
    val bufSize = 2048

    //WAV format
    val format = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        44100F,
        16,
        2,
        4,
        44100F,
        false
    )
    val info = DataLine.Info(TargetDataLine::class.java, format) // format is an AudioFormat object

    return if (!AudioSystem.isLineSupported(info)) {
        logger.error { "Line not supported" }
        null
    } else {
        // Obtain and open the line.
        line = AudioSystem.getLine(info) as TargetDataLine
        line.open(format, bufSize)
        line.start()

        val buf = ByteArray(bufSize)
        val samples = DoubleArray(bufSize / 2)
        val b = line.read(buf, 0, buf.size)
        while (b > -1) {

            // convert bytes to samples here
            var i = 0
            var s = 0
            while (i < b) {
                var sample = 0

                sample = sample or ((buf[i++] and 0xFF.toByte()).toInt()) // (reverse these two lines
                sample = sample or (buf[i++].toInt() shl 8)   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768.0
            }
        }

        var peak = 0.0
        var rms = 0.0

        for (sample in samples) {

            val abs = Math.abs(sample)
            if (abs > peak) {
                peak = abs
            }

            rms += sample * sample
        }

        Pair(BigDecimal(Math.sqrt(rms / samples.size)), BigDecimal(peak))
    }
}