/**
 * 文件上传工具 - 供资料库和任务管理模块共用
 * 注意：所有路径都是前端路径，调用的是后端API
 */

/**
 * 基础文件上传方法
 * @param {File} file - 要上传的文件对象
 * @param {string} subject - 学科分类 (如: 'math', 'chinese')
 * @param {string} type - 文件类型 (如: 'task', 'material')
 * @returns {Promise<Object>} 上传结果 (包含 fileName, fileDownloadUri 等)
 */
const baseUpload = async (file, subject, type) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('subject', subject);
    formData.append('type', type);

    const token = localStorage.getItem('accessToken');
    const headers = {};
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch('/api/files/upload', {
        method: 'POST',
        headers: headers,
        body: formData
    });

    if (!response.ok) {
        throw new Error(`上传失败: ${response.statusText}`);
    }

    return await response.json();
};

// ================= 提供给外部使用的接口 =================
/**
 * 供任务管理模块调用的上传接口
 * @param {File} file - 任务相关文件
 * @param {string} subject - 任务所属学科
 * @returns {Promise<Object>} 上传结果
 */
export const uploadForTask = (file, subject) => {
    return baseUpload(file, subject, 'task'); // 固定type为'task'
};

/**
 * 供资料库模块调用的上传接口
 * @param {File} file - 资料文件
 * @param {string} subject - 资料所属学科
 * @param {string} type - 资料类型 (如: 'textbook', 'notes')
 * @returns {Promise<Object>} 上传结果
 */
export const uploadForLibrary = (file, subject, type) => {
    return baseUpload(file, subject, type || 'material');
};

export default {
    uploadForTask,
    uploadForLibrary
};
