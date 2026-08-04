/**
 * GWLogger - Guidewire PolicyCenter TypeScript Method Logger
 *
 * Logs method entry events with timestamp, class name, and method name.
 * Sends to /api/log endpoint so the Java server can write them to logs/app.log.
 */
export class GWLogger {
    private static readonly PREFIX = '[GW-TS-LOG]';

    /**
     * Log a method entry. Called at the top of each instrumented method.
     * @param className  - The TypeScript class name
     * @param methodName - The method being entered
     * @param args       - Optional extra info to include in the log
     */
    static log(className: string, methodName: string, args?: string): void {
        const ts = new Date().toISOString();
        const msg = args
            ? `${GWLogger.PREFIX} [${ts}] → ${className}.${methodName}(${args})`
            : `${GWLogger.PREFIX} [${ts}] → ${className}.${methodName}()`;
        console.log(msg);
        // Async fire-and-forget to backend log endpoint
        if (typeof fetch !== 'undefined') {
            fetch('/api/log', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ level: 'FINE', source: 'TypeScript', className, methodName, message: msg })
            }).catch(() => {/* silent fail */});
        }
    }

    static info(className: string, message: string): void {
        const ts = new Date().toISOString();
        const msg = `${GWLogger.PREFIX} [${ts}] [INFO ] ${className}: ${message}`;
        console.info(msg);
    }

    static warn(className: string, message: string): void {
        const ts = new Date().toISOString();
        const msg = `${GWLogger.PREFIX} [${ts}] [WARN ] ${className}: ${message}`;
        console.warn(msg);
    }

    static error(className: string, message: string, err?: unknown): void {
        const ts = new Date().toISOString();
        const errMsg = err instanceof Error ? err.message : String(err ?? '');
        const msg = `${GWLogger.PREFIX} [${ts}] [ERROR] ${className}: ${message}${errMsg ? ' | ' + errMsg : ''}`;
        console.error(msg);
    }
}
