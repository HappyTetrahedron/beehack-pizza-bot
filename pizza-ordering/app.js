const puppeteer = require('puppeteer');

const orders = [
    { articleId: 1, count: 1 },
    { articleId: 95, count: 1 }
];

(async () => {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();
    await page.goto('https://www.dieci.ch/en/index');
    await page.type('#plzEntry', '8037');
    await page.click('#orderPizza')
    await page.screenshot({path: '1.png'});
    await page.waitForSelector('.article-container')
    await Promise.all(orders.map(async({ articleId, count }) => {
        const sizeDropdownOption = await page.$(`.dropdown-menu .choose-article[data-article-id="${articleId}"]`);
        const { productElement, dropdown, addToCartButton } = await page.evaluate(dropdownOption => {
            const productElement = dropdownOption.closest("[data-article-number]");
            const dropdown = productElement.find(".dropdown-toggle");
            const addToCartButton = productElement.find(".article-price-and-choose-btn .choose-article");
            return { productElement, dropdown, addToCartButton };
        }, sizeDropdownOption);
        //
        // await dropdown.click();
        // await sizeDropdownOption.click();
        // await addToCartButton.click();
        // await page.screenshot({path: `${articleId}.png`});
    }));

    await browser.close();
})();