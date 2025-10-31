const test = [
    {
        path: '/test',
        name: 'test',
        component: () => import('../layouts/admin-layout.vue'),
        children: [
            {
                path: 'test-upload-image',
                name: 'test-upload-image',
                component: () => import('../views/viewTest/testUploadImage.vue')
            }
        ]
    }
]

export default test;