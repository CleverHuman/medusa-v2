/**
 * Logger Utility for Co-op Member Portal
 * 根据环境自动控制日志输出
 * DEV 环境：显示所有日志
 * Staging/Production 环境：不显示调试日志，只记录错误到远程服务（可选）
 */

(function() {
    'use strict';

    // 检测当前环境
    const hostname = window.location.hostname;
    const isDevelopment = hostname === 'localhost' || hostname === '127.0.0.1' || hostname.includes('.local');
    
    // 判断是否为 staging 环境（可以根据实际域名调整）
    const isStaging = hostname.includes('staging') || hostname.includes('test') || hostname.includes('dev');
    
    // 生产环境：既不是开发环境也不是 staging 环境
    const isProduction = !isDevelopment && !isStaging;
    
    // 是否启用日志（仅在开发环境）
    const ENABLE_LOGS = isDevelopment;

    /**
     * 安全的错误消息提取
     * 不包含堆栈信息和技术细节
     */
    function getSafeErrorMessage(error) {
        if (!error) return 'An error occurred';
        
        const message = error.message || String(error);
        
        // 过滤掉可能泄露信息的错误消息
        if (message.includes('stack') || message.includes('at ') || message.includes('file://')) {
            return 'An error occurred. Please try again.';
        }
        
        // 网络相关错误
        if (message.includes('Network') || message.includes('Failed to fetch')) {
            return 'Network error. Please check your connection.';
        }
        
        if (message.includes('timeout')) {
            return 'Request timeout. Please try again.';
        }
        
        // 如果是开发环境，返回完整消息；否则返回通用消息
        if (ENABLE_LOGS) {
            return message;
        }
        
        return 'An error occurred. Please try again.';
    }

    /**
     * 安全的错误对象（不包含堆栈信息）
     */
    function getSafeErrorObject(error) {
        if (!error) return null;
        
        const safeError = {
            name: error.name || 'Error',
            message: getSafeErrorMessage(error)
        };
        
        // 仅在开发环境包含堆栈信息
        if (ENABLE_LOGS && error.stack) {
            safeError.stack = error.stack;
        }
        
        return safeError;
    }

    /**
     * Logger 对象
     */
    const Logger = {
        isDevelopment: isDevelopment,
        isStaging: isStaging,
        isProduction: isProduction,
        enabled: ENABLE_LOGS,

        /**
         * 普通日志（仅在开发环境）
         */
        log(...args) {
            if (ENABLE_LOGS) {
                console.log(...args);
            }
        },

        /**
         * 信息日志（仅在开发环境）
         */
        info(...args) {
            if (ENABLE_LOGS) {
                console.info(...args);
            }
        },

        /**
         * 警告日志（仅在开发环境）
         */
        warn(...args) {
            if (ENABLE_LOGS) {
                console.warn(...args);
            }
        },

        /**
         * 错误日志
         * 开发环境：完整输出
         * 生产环境：只记录到远程服务（可选），不输出到控制台
         */
        error(...args) {
            if (ENABLE_LOGS) {
                // 开发环境：完整输出
                console.error(...args);
            } else {
                // 生产环境：可以选择发送到远程日志服务
                // 这里暂时不实现，避免额外的网络请求
                // 如果需要，可以在这里添加远程日志收集
                // this.sendToLoggingService('error', args);
            }
        },

        /**
         * 调试日志（仅在开发环境）
         */
        debug(...args) {
            if (ENABLE_LOGS) {
                console.debug(...args);
            }
        },

        /**
         * 安全地记录错误（不包含堆栈信息）
         */
        errorSafe(error, context = '') {
            const safeError = getSafeErrorObject(error);
            if (ENABLE_LOGS) {
                if (context) {
                    console.error(`[${context}]`, safeError);
                } else {
                    console.error(safeError);
                }
            }
            return safeError;
        },

        /**
         * 获取安全的错误消息（用于显示给用户）
         */
        getSafeMessage: getSafeErrorMessage,

        /**
         * 记录 API 响应（仅在开发环境，且不记录敏感数据）
         */
        logResponse(endpoint, response, includeData = false) {
            if (ENABLE_LOGS) {
                if (includeData) {
                    console.log(`[API] ${endpoint}:`, response);
                } else {
                    console.log(`[API] ${endpoint}:`, response.code, response.msg || 'OK');
                }
            }
        },

        /**
         * 记录 API 错误（安全版本）
         */
        logApiError(endpoint, error) {
            const safeError = this.errorSafe(error, `API:${endpoint}`);
            return safeError;
        }
    };

    // 导出到全局
    window.Logger = Logger;
    
    // 在开发环境输出环境信息
    if (ENABLE_LOGS) {
        console.log('🔧 Logger initialized - Environment:', {
            hostname: hostname,
            isDevelopment: isDevelopment,
            isStaging: isStaging,
            isProduction: isProduction,
            logsEnabled: ENABLE_LOGS
        });
    }
})();

