/**
 * Color Mapper Utility
 * Maps hex color codes to Vietnamese color names
 */

// Database of standard Vietnamese color names
const COLOR_DATABASE = [
    { hex: '#FF0000', name: 'Đỏ' },
    { hex: '#DC143C', name: 'Đỏ thẫm' },
    { hex: '#FF6347', name: 'Đỏ cam' },
    { hex: '#FFA500', name: 'Cam' },
    { hex: '#FF8C00', name: 'Cam đậm' },
    { hex: '#FFD700', name: 'Vàng gold' },
    { hex: '#FFFF00', name: 'Vàng' },
    { hex: '#ADFF2F', name: 'Vàng lục' },
    { hex: '#00FF00', name: 'Xanh lá' },
    { hex: '#32CD32', name: 'Xanh lá cây' },
    { hex: '#008000', name: 'Xanh lá đậm' },
    { hex: '#00FFFF', name: 'Xanh cyan' },
    { hex: '#00CED1', name: 'Xanh ngọc' },
    { hex: '#1E90FF', name: 'Xanh dương' },
    { hex: '#0000FF', name: 'Xanh đậm' },
    { hex: '#000080', name: 'Xanh navy' },
    { hex: '#4B0082', name: 'Chàm' },
    { hex: '#800080', name: 'Tím' },
    { hex: '#9370DB', name: 'Tím nhạt' },
    { hex: '#FF00FF', name: 'Hồng tím' },
    { hex: '#FFC0CB', name: 'Hồng' },
    { hex: '#FF69B4', name: 'Hồng đậm' },
    { hex: '#FFB6C1', name: 'Hồng nhạt' },
    { hex: '#FFFFFF', name: 'Trắng' },
    { hex: '#F5F5F5', name: 'Trắng xám' },
    { hex: '#D3D3D3', name: 'Xám nhạt' },
    { hex: '#C0C0C0', name: 'Bạc' },
    { hex: '#808080', name: 'Xám' },
    { hex: '#696969', name: 'Xám đậm' },
    { hex: '#000000', name: 'Đen' },
    { hex: '#A52A2A', name: 'Nâu' },
    { hex: '#8B4513', name: 'Nâu đậm' },
    { hex: '#D2691E', name: 'Nâu nhạt' },
    { hex: '#F5DEB3', name: 'Be' },
    { hex: '#FFE4C4', name: 'Be nhạt' },
]

/**
 * Convert hex color to RGB
 * @param {string} hex - Hex color code (e.g., #FF5733)
 * @returns {object} RGB object { r, g, b }
 */
function hexToRgb(hex) {
    // Remove # if present
    hex = hex.replace('#', '')

    // Parse hex to RGB
    const result = /^([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)

    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : { r: 0, g: 0, b: 0 }
}

/**
 * Calculate Euclidean distance between two RGB colors
 * @param {object} rgb1 - First RGB color
 * @param {object} rgb2 - Second RGB color
 * @returns {number} Distance value
 */
function colorDistance(rgb1, rgb2) {
    return Math.sqrt(
        Math.pow(rgb1.r - rgb2.r, 2) +
        Math.pow(rgb1.g - rgb2.g, 2) +
        Math.pow(rgb1.b - rgb2.b, 2)
    )
}

/**
 * Get Vietnamese color name for a given hex code
 * Maps to the closest color in the database
 * @param {string} hex - Hex color code (e.g., #FF5733)
 * @returns {string} Vietnamese color name
 */
export function getVietnameseColorName(hex) {
    if (!hex || typeof hex !== 'string') {
        return 'Không xác định'
    }

    // Ensure hex has # prefix
    if (!hex.startsWith('#')) {
        hex = '#' + hex
    }

    // Validate hex format
    if (!/^#[0-9A-F]{6}$/i.test(hex)) {
        return 'Không hợp lệ'
    }

    const inputRgb = hexToRgb(hex)
    let closestColor = COLOR_DATABASE[0]
    let minDistance = Infinity

    // Find closest color by Euclidean distance
    for (const color of COLOR_DATABASE) {
        const colorRgb = hexToRgb(color.hex)
        const distance = colorDistance(inputRgb, colorRgb)

        if (distance < minDistance) {
            minDistance = distance
            closestColor = color
        }
    }

    return closestColor.name
}

/**
 * Validate hex color format
 * @param {string} hex - Hex color code
 * @returns {boolean} True if valid
 */
export function isValidHexColor(hex) {
    if (!hex || typeof hex !== 'string') return false

    // Remove # if present
    const cleanHex = hex.replace('#', '')

    // Check if it's 6 characters and all are hex digits
    return /^[0-9A-F]{6}$/i.test(cleanHex)
}

/**
 * Normalize hex color (ensure # prefix and uppercase)
 * @param {string} hex - Hex color code
 * @returns {string} Normalized hex code
 */
export function normalizeHexColor(hex) {
    if (!hex) return '#000000'

    let cleanHex = hex.replace('#', '').toUpperCase()

    // Ensure 6 characters
    if (cleanHex.length === 3) {
        cleanHex = cleanHex.split('').map(c => c + c).join('')
    }

    return '#' + cleanHex
}

/**
 * Get all available colors
 * @returns {array} Array of color objects
 */
export function getAllColors() {
    return COLOR_DATABASE.map(color => ({
        ...color,
        label: `${color.name} (${color.hex})`
    }))
}
