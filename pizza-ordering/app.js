const puppeteer = require('puppeteer');

const orders = [
    { articleId: 1, count: 1 },
    { articleId: 492, count: 1 }
];

(async () => {
    const browser = await puppeteer.launch({ headless: false });
    const page = await browser.newPage();
    await page.setViewport({ width: 2000, height: 1500});
    await page.goto('https://www.dieci.ch/en/index');
    await page.type('#plzEntry', '8037');
    await page.click('#orderPizza')
    await page.waitForSelector('.article-container')
    await page.screenshot({path: 'a.png'});
    await Promise.all(orders.map(async({ articleId, count }) => {
        const sizeDropdownOptionSelector = `.dropdown-menu .choose-article[data-article-id="${articleId}"]`;
        const sizeDropdownOption = await page.$(sizeDropdownOptionSelector);
        const productId = await page.evaluate(
            sizeDropdown => sizeDropdown.closest("[data-article-number]").getAttribute("data-article-number")
        , sizeDropdownOption);
        const productElement = await page.$(`[data-article-number="${productId}"]`);
        // console.log(productElement);
        const dropdown = await productElement.$(".dropdown-toggle");
        const addToCartButton = await productElement.$(".article-price-and-choose-btn :not(.choose-toppings) .choose-article");

        await dropdown.click()
        await page.waitForSelector(sizeDropdownOptionSelector, { visible: true, timeout: 2000 });
        await sizeDropdownOption.click();
        await page.screenshot({path: 'b.png'});
        await addToCartButton.click();
        await page.screenshot({path: `${articleId}.png`});

    }));

    await browser.close();
})();